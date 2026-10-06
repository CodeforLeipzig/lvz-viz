package de.codefor.le.crawler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;

import de.codefor.le.model.PoliceTicker;
import de.codefor.le.utilities.Utils;
import lombok.RequiredArgsConstructor;

/**
 * Crawls the concrete url of an article to extract the following information into a <code>PoliceTicker</code> model:
 * <ul>
 * <li>title</li>
 * <li>url</li>
 * <li>article</li>
 * <li>snippet (20 words and three points)</li>
 * <li>copyright</li>
 * <li>date published</li>
 * </ul>
 * The date published is taken from the JSON-LD <code>NewsArticle</code> of the page, the copyright falls back to it.
 */
@Component
@RequiredArgsConstructor
public class LvzPoliceTickerDetailViewCrawler implements DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(LvzPoliceTickerDetailViewCrawler.class);

    private static final String LOG_ELEMENT_FOUND = "Element '{}' found with selector '{}' for article.";

    private static final String LOG_ELEMENT_NOT_FOUND = "Element '{}' not found for article.";

    private static final String LOG_ELEMENT_FOUND_IN_JSON_LD = "Element '{}' found in JSON-LD for article.";

    private static final String ARTICLE_ALSO_READ = "Lesen Sie auch";

    private static final String ARTICLE_READ_MORE = "Mehr aus dem Polizeiticker";

    private static final String ARTICLE_HEAD_SELECTOR = "div[class*=ArticleHeadstyled__ArticleHeadHeadlineContainer]";

    /** Headline of the page the bot detection serves instead of the article. */
    private static final String BLOCK_PAGE_TEXT = "Der Zugriff ist vorübergehend eingeschränkt";

    /** The block page may also be embedded as an iframe from the bot detection vendor. */
    private static final String BLOCK_PAGE_IFRAME_SELECTOR = "iframe[src*=captcha-delivery.com]";

    /** Structured data for search engines, also present on paid articles where the visible meta block is missing. */
    private static final String JSON_LD_SELECTOR = "script[type=application/ld+json]";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String LVZ_PUBLISHER_NAME = "Leipziger Volkszeitung";

    private static final String LVZ_PUBLISHER_ABBREVIATION = "LVZ";

    private final CrawlerWebDriverFactory webDriverFactory;

    private WebDriver driver;

    /**
     * Closes the browser once a batch of detail pages is done. Keeping it open until the next
     * scheduler run would hit the idle session timeout of the selenium node.
     * <p>
     * Synchronized like {@link #crawl(String)}: the driver is created on an async worker thread but
     * closed by the scheduler thread, and one session must never be shared concurrently.
     */
    public synchronized void closeBrowser() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    private void closeBrowserQuietly() {
        try {
            closeBrowser();
        } catch (final WebDriverException e) {
            logger.debug("Closing the broken browser session failed", e);
            driver = null;
        }
    }

    @Override
    public void destroy() {
        closeBrowser();
    }

    @Async
    public Future<PoliceTicker> execute(final String url) {
        logger.debug("Start crawling detail page {}.", url);
        final var watch = Stopwatch.createStarted();
        try {
            return new AsyncResult<>(crawl(url));
        } finally {
            watch.stop();
            logger.debug("Finished crawling detail page in {} ms.", watch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    /**
     * Crawl concrete url for one ticker article.
     *
     * @param url article url
     * @return PoliceTicker
     */
    private synchronized PoliceTicker crawl(final String url) {
        // A plain HTTP client gets a 403 here: the bot detection rejects it on the TLS
        // fingerprint, so neither headers nor cookies help. Only a real browser gets through.
        if (driver == null) {
            driver = webDriverFactory.create();
        }
        try {
            driver.get(url);
        } catch (final WebDriverException e) {
            // the session may be gone (e.g. a crashed selenium node). Drop it, so the next article
            // starts a fresh one instead of failing the same way for the rest of the batch.
            closeBrowserQuietly();
            throw e;
        }
        // the article is rendered client-side, so wait (via the implicit timeout) for the headline
        // before serializing the DOM. Without it we would happily parse a blocking page into an
        // article with empty fields.
        final var headlineMissing = driver.findElements(By.cssSelector(ARTICLE_HEAD_SELECTOR)).isEmpty();
        final var doc = Jsoup.parse(driver.getPageSource(), url);
        if (headlineMissing) {
            if (isBlockPage(doc)) {
                logger.warn("blocked by the bot detection at {}", url);
                WebDriverScreenshot.take(driver, WebDriverScreenshot.REASON_BLOCKED);
                throw new CrawlerBlockedException("blocked by the bot detection at " + url);
            }
            logger.warn("article headline not found for {} (page title: {})", url, driver.getTitle());
            WebDriverScreenshot.take(driver, WebDriverScreenshot.REASON_NO_SUCH_ELEMENT);
            throw new IllegalStateException("article headline not found for " + url + ", page title: " + driver.getTitle());
        }
        final PoliceTicker result = convertToDataModel(doc);
        // the container above can be present while the extraction still yields nothing, e.g. after
        // a markup change. Without a title the article is useless, so do not let it reach the index.
        if (Strings.isNullOrEmpty(result.getTitle())) {
            logger.warn("no title extracted for {} (page title: {})", url, driver.getTitle());
            WebDriverScreenshot.take(driver, WebDriverScreenshot.REASON_NO_SUCH_ELEMENT);
            throw new IllegalStateException("no title extracted for " + url);
        }
        result.setUrl(url);
        result.setId(Utils.generateHashForUrl(url));
        logger.info("Crawled {}.", url);
        if (logger.isDebugEnabled()) {
            logger.debug("Extracted {}.", result);
        }
        return result;
    }

    private static boolean isBlockPage(final Document doc) {
        return doc.text().contains(BLOCK_PAGE_TEXT) || !doc.select(BLOCK_PAGE_IFRAME_SELECTOR).isEmpty();
    }

    /**
     * mapper to map the information of the document into a model
     *
     * @param doc the document
     * @return the model with all information which are needed
     */
    private static PoliceTicker convertToDataModel(final Document doc) {
        final var dm = new PoliceTicker();
        final var newsArticle = findNewsArticle(doc);
        extractTitle(doc, dm);
        extractArticle(doc, dm, newsArticle);
        extractTeaser(doc, dm);
        extractCopyright(doc, dm, newsArticle);
        extractDatePublished(doc, dm, newsArticle);
        return dm;
    }

    /**
     * Finds the JSON-LD block describing the article itself. The page has others too, e.g. for the breadcrumbs.
     */
    private static Optional<JsonNode> findNewsArticle(final Document doc) {
        for (final var script : doc.select(JSON_LD_SELECTOR)) {
            final JsonNode json;
            try {
                json = objectMapper.readTree(script.data());
            } catch (final JsonProcessingException e) {
                logger.debug("Skipping unparsable JSON-LD block", e);
                continue;
            }
            final Iterable<JsonNode> candidates = json.isArray() ? json : List.of(json);
            for (final var node : candidates) {
                if ("NewsArticle".equals(node.path("@type").asText())) {
                    return Optional.of(node);
                }
            }
        }
        return Optional.empty();
    }

    private static void extractTitle(final Document doc, final PoliceTicker dm) {
        final var title = "title";
        final var cssQuery = "div[class*=ArticleHeadstyled__ArticleHeadHeadlineContainer] > h2[class*=Headlinestyled__Headline]";
        final var elem = doc.selectFirst(cssQuery);
        if (elem != null) {
            logger.debug(LOG_ELEMENT_FOUND, title, cssQuery);
            dm.setTitle(elem.ownText());
        }
        if (Strings.isNullOrEmpty(dm.getTitle())) {
            logger.warn(LOG_ELEMENT_NOT_FOUND, title);
        }
    }

    /**
     * Extracts the copyright information from the given document and updates the provided PoliceTicker object.
     * It is searched at the end of the article first, then in the ArticleMeta section. Paid articles have neither,
     * so the publisher from the JSON-LD is the last resort.
     */
    private static void extractCopyright(final Document doc, final PoliceTicker dm, final Optional<JsonNode> newsArticle) {
        final var copyright = "copyright";
        for (final var cssQuery : List.of("#contentMain p[class*=Editorialstyled__Editorial]",
                "div[class*=ArticleMetastyled__ArticleMeta] > div[class*=Stackstyled__Stack] > address")) {
            final var elem = doc.selectFirst(cssQuery);
            if (elem != null && elem.hasText()) {
                logger.debug(LOG_ELEMENT_FOUND, copyright, cssQuery);
                dm.setCopyright(elem.text());
                return;
            }
        }
        final var publisher = newsArticle.map(node -> node.path("publisher").path("name").asText()).orElse("");
        if (publisher.isBlank()) {
            logger.warn(LOG_ELEMENT_NOT_FOUND, copyright);
        } else {
            logger.debug(LOG_ELEMENT_FOUND_IN_JSON_LD, copyright);
            dm.setCopyright(shortenPublisher(publisher));
        }
    }

    /**
     * The Editorial line of free articles credits the LVZ by its abbreviation, while the JSON-LD spells it out.
     */
    @VisibleForTesting
    static String shortenPublisher(final String publisher) {
        final var name = publisher.trim();
        return LVZ_PUBLISHER_NAME.equals(name) ? LVZ_PUBLISHER_ABBREVIATION : name;
    }

    /**
     * Extracts the publishing date from the JSON-LD, or from the ArticleMeta section if the JSON-LD has none.
     * Paid articles lack the ArticleMeta section.
     */
    private static void extractDatePublished(final Document doc, final PoliceTicker dm, final Optional<JsonNode> newsArticle) {
        final var publishingDate = "datePublished";
        newsArticle.ifPresent(node -> dm.setDatePublished(extractDate(node.path(publishingDate).asText())));
        if (dm.getDatePublished() != null) {
            logger.debug(LOG_ELEMENT_FOUND_IN_JSON_LD, publishingDate);
            return;
        }
        final var cssQuery = "div[class*=ArticleMetastyled__ArticleMeta] > div[class*=Stackstyled__Stack] > time";
        final var elem = doc.selectFirst(cssQuery);
        if (elem != null) {
            logger.debug(LOG_ELEMENT_FOUND, publishingDate, cssQuery);
            dm.setDatePublished(extractDate(elem.attr("datetime")));
        }
        if (dm.getDatePublished() == null) {
            logger.warn(LOG_ELEMENT_NOT_FOUND, publishingDate);
        }
    }

    @VisibleForTesting
    static Date extractDate(final String date) {
        logger.debug("extractDate from {}", date);
        Date result = null;
        if (!Strings.isNullOrEmpty(date)) {
            try {
                // only a timestamp without any zone is local time; anything else carries its own
                // offset. The site renders the same article as either "...Z" or "...+02:00",
                // so both have to end up at the same instant.
                final var zonedDateTime = date.length() == 19
                        ? LocalDateTime.parse(date).atZone(ZoneId.of("Europe/Berlin"))
                        : ZonedDateTime.parse(date);
                result = Date.from(zonedDateTime.toInstant());
            } catch (final DateTimeParseException e) {
                logger.warn(e.toString(), e);
            }
        }
        return result;
    }

    private static void extractArticle(final Document doc, final PoliceTicker dm, final Optional<JsonNode> newsArticle) {
        final var article = "article";
        var cssQuery = "article > nav + div + div p";
        extractArticle(doc, dm, cssQuery);
        if (Strings.isNullOrEmpty(dm.getArticle())) {
            if (isPaidContent(doc, newsArticle)) {
                logger.info(LOG_ELEMENT_NOT_FOUND + " Detected paid content.", article);
            } else {
                logger.warn(LOG_ELEMENT_NOT_FOUND, article);
            }
        } else {
            logger.debug(LOG_ELEMENT_FOUND, article, cssQuery);
        }
    }

    /**
     * The paid icon alone is no proof: teasers of related paid articles on a free article carry it as well.
     */
    private static boolean isPaidContent(final Document doc, final Optional<JsonNode> newsArticle) {
        return newsArticle.map(node -> node.get("isAccessibleForFree"))
                .map(free -> !free.asBoolean())
                .orElseGet(() -> doc.selectFirst("title#paid-icon") != null);
    }

    private static boolean extractArticle(final Document doc, final PoliceTicker dm, final String cssQuery) {
        final var elements = doc.select(cssQuery);
        if (!elements.isEmpty()) {
            dm.setArticle(extractArticle(elements));
            return true;
        }
        return false;
    }

    private static String extractArticle(final Elements elements) {
        final var article = new StringBuilder();
        for (final var e : elements) {
            if (e.hasText() && !includesReadMore(e.text())) {
                if (article.length() > 0) {
                    article.append(" ");
                }
                article.append(e.text());
            }
        }
        return article.toString();
    }

    private static boolean includesReadMore(final String text) {
        return Stream.of(ARTICLE_ALSO_READ, ARTICLE_READ_MORE).anyMatch(text::startsWith);
    }

    private static void extractTeaser(final Document doc, final PoliceTicker dm) {
        final var teaser = "teaser";
        final var cssQuery = "div[class*=ArticleHeadstyled__ArticleTeaserContainer] > p";
        final var elem = doc.selectFirst(cssQuery);
        if (elem != null) {
            logger.debug(LOG_ELEMENT_FOUND, teaser, cssQuery);
            dm.setSnippet(elem.ownText().trim());
        }
        if (Strings.isNullOrEmpty(dm.getSnippet())) {
            logger.warn(LOG_ELEMENT_NOT_FOUND, teaser);
        }
    }
}
