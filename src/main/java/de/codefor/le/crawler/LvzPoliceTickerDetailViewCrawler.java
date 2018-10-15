package de.codefor.le.crawler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

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
 *
 * @author sepe81
 * @author spinner0815
 */
@Component
@Profile("crawl")
public class LvzPoliceTickerDetailViewCrawler {

    private static final Logger logger = LoggerFactory.getLogger(LvzPoliceTickerDetailViewCrawler.class);

    private static final int WAIT_BEFORE_EACH_ACCESS_TO_PREVENT_BANNING = 50;

    private static final String LOG_ELEMENT_FOUND = "Element '{}' found with selector '{}' for article.";

    private static final String LOG_ELEMENT_NOT_FOUND = "Element '{}' not found for article.";

    @Async
    public Future<Iterable<PoliceTicker>> execute(final Iterable<String> detailURLs) {
        final Stopwatch watch = Stopwatch.createStarted();
        logger.info("Start crawling detail pages.");
        final List<PoliceTicker> policeTickers = new ArrayList<>();
        try {
            for (final String url : detailURLs) {
                final PoliceTicker ticker = crawl(url);
                if (ticker != null) {
                    policeTickers.add(ticker);
                }
                try {
                    Thread.sleep(WAIT_BEFORE_EACH_ACCESS_TO_PREVENT_BANNING);
                } catch (final InterruptedException e) {
                    logger.error(e.toString(), e);
                }
            }
        } finally {
            watch.stop();
            logger.info("Finished crawling {} detail pages in {} ms.", policeTickers.size(), watch.elapsed(TimeUnit.MILLISECONDS));
        }
        return new AsyncResult<>(policeTickers);
    }

    /**
     * Crawl concrete url for one ticker article.
     *
     * @param url article url
     * @return PoliceTickers
     */
    private static PoliceTicker crawl(final String url) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent(LvzPoliceTickerCrawler.USER_AGENT).timeout(LvzPoliceTickerCrawler.REQUEST_TIMEOUT).get();
        } catch (final IOException e) {
            logger.error(e.toString(), e);
        }
        PoliceTicker result = null;
        if (doc != null) {
            result = convertToDataModel(doc);
            result.setUrl(url);
            result.setId(Utils.generateHashForUrl(url));
            logger.info("Crawled {}.", url);
        }
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
        final PoliceTicker dm = new PoliceTicker();
        extractTitle(doc, dm);
        extractArticle(doc, dm);
        extractTeaser(doc, dm);
        extractCopyright(doc, dm);
        extractDatePublished(doc, dm);
        return dm;
    }

    private static void extractTitle(final Document doc, final PoliceTicker dm) {
        final String title = "title";
        final String cssQuery = ".pdb-article-teaser-breadcrumb-headline-title";
        final Element elem = doc.selectFirst(cssQuery);
        if (elem != null) {
            logger.debug(LOG_ELEMENT_FOUND, title, cssQuery);
            dm.setTitle(elem.ownText());
        }
        if (Strings.isNullOrEmpty(dm.getTitle())) {
            logger.warn(LOG_ELEMENT_NOT_FOUND, title);
        }
    }

    private static void extractCopyright(final Document doc, final PoliceTicker dm) {
        final String copyright = "copyright";
        final String cssQuery = "li:contains(©)";
        final Element elem = doc.selectFirst(cssQuery);
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
        final String publishingDate = "publishing date";
        final String cssQuery = ".pdb-article > script[type=application/ld+json]";
        final Element elem = doc.selectFirst(cssQuery);
        if (elem != null) {
            logger.debug(LOG_ELEMENT_FOUND, publishingDate, cssQuery);
            final String date = elem.data();
            final int startIndex = date.indexOf("datePublished") + 17;
            dm.setDatePublished(extractDate(date.substring(startIndex, startIndex + 25)));
        }
        if (dm.getDatePublished() == null) {
            logger.warn(LOG_ELEMENT_NOT_FOUND, publishingDate);
        }
    }

    static Date extractDate(final String date) {
        Date result = null;
        if (!Strings.isNullOrEmpty(date)) {
            ZonedDateTime zonedDateTime = null;
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
        final String content = "articlecontent";
        final String cssQuery = ".pdb-article-body > .pdb-richtext-field > p";
        final Elements elements = doc.select(cssQuery);
        if (!elements.isEmpty()) {
            logger.debug(LOG_ELEMENT_FOUND, content, cssQuery);
        }

        final StringBuilder article = new StringBuilder();
        for (final Element e : elements) {
            if (e.hasText()) {
                if (article.length() > 0) {
                    article.append(" ");
                }
                article.append(e.text());
            }
        }
        dm.setArticle(article.toString());

        if (Strings.isNullOrEmpty(dm.getArticle())) {
            logger.warn(LOG_ELEMENT_NOT_FOUND, content);
        }
    }

    private static void extractTeaser(final Document doc, final PoliceTicker dm) {
        final String teaser = "teaser";
        final String cssQuery = ".pdb-article-teaser-intro > .pdb-richtext-field > p";
        final Element elem = doc.selectFirst(cssQuery);
        if (elem != null) {
            logger.debug(LOG_ELEMENT_FOUND, teaser, cssQuery);
            dm.setSnippet(elem.ownText().trim());
        }
        if (Strings.isNullOrEmpty(dm.getSnippet())) {
            logger.warn(LOG_ELEMENT_NOT_FOUND, teaser);
        }
    }
}
