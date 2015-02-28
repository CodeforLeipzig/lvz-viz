package de.codefor.le.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

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

    @Async
    public Future<List<PoliceTicker>> execute(final List<String> detailURLs) {
        logger.info("Start crawling the detailed pages");
        final List<PoliceTicker> policeTickers = new ArrayList<>();
        for (final String url : detailURLs) {
            try {
                Thread.sleep(WAIT_BEFORE_EACH_ACCESS_TO_PREVENT_BANNING);
                final PoliceTicker ticker = crawl(url);
                if (ticker != null) {
                    policeTickers.add(ticker);
                }
            } catch (final InterruptedException e) {
                logger.error(e.toString(), e);
            }
        }
        return new AsyncResult<List<PoliceTicker>>(policeTickers);
    }

    /**
     * Crawl concrete url for one ticker article.
     *
     * @param url article url
     * @return PoliceTickers
     */
    private PoliceTicker crawl(final String url) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent(LvzPoliceTickerCrawler.USER_AGENT)
                    .timeout(LvzPoliceTickerCrawler.REQUEST_TIMEOUT).get();
        } catch (final IOException e) {
            logger.error(e.toString(), e);
        }
        PoliceTicker result = null;
        if (doc != null) {
            result = convertToDataModel(doc);
            result.setUrl(url);
            result.setId(Utils.generateHashForUrl(url));
            result.setArticleId(Utils.extractArticleId(url));
            logger.info("Crawled {}", url);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Crawled {}", result);
        }
        return result;
    }

    /**
     * mapper to map the information of the document into a model
     *
     * @param doc the document
     * @return the model with all information which are needed
     */
    private PoliceTicker convertToDataModel(final Document doc) {
        final PoliceTicker dm = new PoliceTicker();
        extractTitle(doc, dm);
        extractArticleAndSnippet(doc, dm);
        extractCopyright(doc, dm);
        extractDatePublished(doc, dm);
        return dm;
    }

    private void extractTitle(final Document doc, final PoliceTicker dm) {
        final String ownText = doc.select("title").first().ownText();
        dm.setTitle(ownText);
    }

    private void extractCopyright(final Document doc, final PoliceTicker dm) {
        final String copyrightAndDatePublished = extractCopyrightAndDatePublished(doc);
        dm.setCopyright(copyrightAndDatePublished.split(",")[0]);
    }

    private void extractDatePublished(final Document doc, final PoliceTicker dm) {
        final String copyrightAndDatePublished = extractCopyrightAndDatePublished(doc);

        final String date = copyrightAndDatePublished.substring(copyrightAndDatePublished.indexOf(",") + 1).trim();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern("dd.MM.YYYY, HH:mm 'Uhr'");

        dm.setDatePublished(DateTime.parse(date, fmt).toDateTimeISO().toDate());
    }

    private String extractCopyrightAndDatePublished(final Document doc) {
        String result = "";
        for (final Element e : doc.select("div.copyright")) {
            // only plain copyright
            if (e.hasText()) {
                result = e.ownText();
            }
        }
        return result;
    }

    private void extractArticleAndSnippet(final Document doc, final PoliceTicker dm) {
        for (final Element e : doc.select("div.ARTIKEL_TEXT")) {
            if (e.hasText()) {
                final String article = e.ownText();
                final String[] split = article.split("\\s");
                final StringBuilder sb = new StringBuilder();
                for (int i = 0; i < Math.min(20, split.length); i++) {
                    sb.append(split[i]).append(" ");
                }
                dm.setArticle(article);
                dm.setSnippet(sb.toString().trim() + "...");
            }
        }
    }
}
