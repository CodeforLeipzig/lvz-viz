package de.codefor.le.crawler;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;

import de.codefor.le.model.PoliceTicker;
import de.codefor.le.utilities.Utils;

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
 */
@Component
public class LvzPoliceTickerDetailViewCrawler {

    private static final Logger logger = LoggerFactory.getLogger(LvzPoliceTickerDetailViewCrawler.class);

    private static final String LOG_ELEMENT_FOUND = "Element '{}' found with selector '{}' for article.";

    private static final String LOG_ELEMENT_NOT_FOUND = "Element '{}' not found for article.";

    private static final String ARTICLE_ALSO_READ = "Lesen Sie auch";

    private static final String ARTICLE_READ_MORE = "Mehr aus dem Polizeiticker";

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
    private static PoliceTicker crawl(final String url) {
        Document doc;
        try {
            doc = Jsoup.connect(url).userAgent(LvzPoliceTickerCrawler.USER_AGENT).timeout(LvzPoliceTickerCrawler.REQUEST_TIMEOUT).get();
        } catch (final IOException e) {
            throw new UncheckedIOException("Request for url " + url + " failed.", e);
        }
        final PoliceTicker result = convertToDataModel(doc);
        result.setUrl(url);
        result.setId(Utils.generateHashForUrl(url));
        logger.info("Crawled {}.", url);
        if (logger.isDebugEnabled()) {
            logger.debug("Extracted {}.", result);
        }
        return result;
    }

    /**
     * mapper to map the information of the document into a model
     *
     * @param doc the document
     * @return the model with all information which are needed
     */
    private static PoliceTicker convertToDataModel(final Document doc) {
        final var dm = new PoliceTicker();
        extractTitle(doc, dm);
        extractArticle(doc, dm);
        extractTeaser(doc, dm);
        extractCopyright(doc, dm);
        extractDatePublished(doc, dm);
        return dm;
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

    private static void extractCopyright(final Document doc, final PoliceTicker dm) {
        final var copyright = "copyright";
        final var cssQuery = "header p[class*=Copyrightstyled__Copyright]";
        final var elem = doc.selectFirst(cssQuery);
        if (elem != null) {
            logger.debug(LOG_ELEMENT_FOUND, copyright, cssQuery);
            dm.setCopyright(elem.text());
        }
        if (Strings.isNullOrEmpty(dm.getCopyright())) {
            logger.warn(LOG_ELEMENT_NOT_FOUND, copyright);
        }
    }

    /**
     * Try to extract the publishing date from a script block.
     *
     * @param doc Document
     * @param dm PoliceTicker
     */
    private static void extractDatePublished(final Document doc, final PoliceTicker dm) {
        final var publishingDate = "datePublished";
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
            ZonedDateTime zonedDateTime;
            try {
                String normalizedDate = date;
                if (date.length() == 20 && date.endsWith("Z")) {
                    normalizedDate = date.substring(0, date.length() - 1);
                }
                if (normalizedDate.length() == 19) {
                    zonedDateTime = LocalDateTime.parse(normalizedDate).atZone(ZoneId.of("Europe/Berlin"));
                } else {
                    zonedDateTime = ZonedDateTime.parse(normalizedDate);
                }
                result = Date.from(zonedDateTime.toInstant());
            } catch (final DateTimeParseException e) {
                logger.warn(e.toString(), e);
            }
        }
        return result;
    }

    private static void extractArticle(final Document doc, final PoliceTicker dm) {
        final var article = "article";
        var cssQuery = "article#article > nav + header + div p";
        extractArticle(doc, dm, cssQuery);
        if (Strings.isNullOrEmpty(dm.getArticle())) {
            if (doc.selectFirst("title#paid-icon") != null) {
                logger.info(LOG_ELEMENT_NOT_FOUND + " Detected paid content.", article);
            } else {
                logger.warn(LOG_ELEMENT_NOT_FOUND, article);
            }
        } else {
            logger.debug(LOG_ELEMENT_FOUND, article, cssQuery);
        }
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
