package de.codefor.le.crawler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * <li>title
 * <li>url
 * <li>article
 * <li>snippet (20words and three points)
 * <li>copyright
 * <li>date published
 * </ul>
 *
 * @author spinner0815
 */
@Component
public class LvzPoliceTickerDetailViewCrawler {

    private static final Logger logger = LoggerFactory.getLogger(LvzPoliceTickerDetailViewCrawler.class);

    private static final int WAIT_BEFORE_EACH_ACCESS_TO_PREVENT_BANNING = 5000;

    private static final String ELLIPSIS = "...";

    @Async
    public Future<Iterable<PoliceTicker>> execute(final Iterable<String> detailURLs) {
        final Stopwatch watch = Stopwatch.createStarted();
        logger.info("Start crawling detail pages");
        final List<PoliceTicker> policeTickers = new ArrayList<>();
        for (final Iterator<String> iterator = detailURLs.iterator(); iterator.hasNext();) {
            final PoliceTicker ticker = crawl(iterator.next());
            if (ticker != null) {
                policeTickers.add(ticker);
            }
            if (iterator.hasNext()) {
                try {
                    Thread.sleep(WAIT_BEFORE_EACH_ACCESS_TO_PREVENT_BANNING);
                } catch (final InterruptedException e) {
                    logger.error(e.toString(), e);
                }
            }
        }
        watch.stop();
        logger.info("Finished crawling {} detail pages in {} ms", policeTickers.size(), watch.elapsed(TimeUnit.MILLISECONDS));
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
            logger.info("Crawled {}", url);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Extracted {}", result);
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
        extractArticleAndSnippet(doc, dm);
        extractCopyright(doc, dm);
        extractDatePublished(doc, dm);
        return dm;
    }

    private static void extractTitle(final Document doc, final PoliceTicker dm) {
        final Element elem = doc.select("h1.pda-entry-title.entry-title").first();
        if (elem != null) {
            dm.setTitle(elem.ownText());
        }
        if (Strings.isNullOrEmpty(dm.getTitle())) {
            logger.warn("title not found for article");
        }
    }

    private static void extractCopyright(final Document doc, final PoliceTicker dm) {
        final Element elem = doc.select("li:contains(©)").first();
        if (elem != null) {
            dm.setCopyright(elem.text());
        }
        if (Strings.isNullOrEmpty(dm.getCopyright())) {
            logger.warn("copyright not found for article");
        }
    }

    private static void extractDatePublished(final Document doc, final PoliceTicker dm) {
        final Element elem = doc.select("span.dtstamp").first();
        if (elem != null) {
            dm.setDatePublished(extractDate(elem.attr("content")));
        }
        if (dm.getDatePublished() == null) {
            logger.warn("publishing date not found for article");
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
                    zonedDateTime = LocalDateTime.parse(normalizedDate).atZone(ZoneId.systemDefault());
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

    private static void extractArticleAndSnippet(final Document doc, final PoliceTicker dm) {
        final StringBuilder article = new StringBuilder();

        for (final Element e : doc.select("#articlecontent > p.pda-abody-p")) {
            if (e.hasText()) {
                if (article.length() > 0) {
                    article.append(" ");
                }
                article.append(e.text());
            }
        }

        dm.setArticle(article.toString());
        final String[] split = dm.getArticle().split("\\s");
        final StringBuilder snippet = new StringBuilder();
        for (int i = 0; i < Math.min(20, split.length); i++) {
            snippet.append(split[i]).append(" ");
        }
        dm.setSnippet(snippet.toString().trim() + ELLIPSIS);

        if (Strings.isNullOrEmpty(dm.getArticle())) {
            logger.warn("content not found for article");
        }
    }
}
