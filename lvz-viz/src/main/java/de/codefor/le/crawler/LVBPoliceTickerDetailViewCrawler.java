package de.codefor.le.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
 * TODO - create a thread which sleeps 5sec after a page was crawled
 * 
 * crawls the detailview to extract and store teh following information into a csv file
 * 
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
public class LVBPoliceTickerDetailViewCrawler {

    private static final Logger logger = LoggerFactory.getLogger(LVBPoliceTickerDetailViewCrawler.class);
    private List<PoliceTicker> policeTickers;

    public LVBPoliceTickerDetailViewCrawler() {
        policeTickers = new ArrayList<>();

    }

    @Async
    public Future<List<PoliceTicker>> execute(List<String> detailURLs) {
        logger.info("start crawling the detailed pages");
        policeTickers = new ArrayList<>();
        try {
            for (String url : detailURLs) {
                Thread.sleep(5000);
                crawl(url);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new AsyncResult<List<PoliceTicker>>(policeTickers);
    }

    // link \t ueberschrift \t inhalt_plain \t copyright \t Datum der Veroeffentlichung
    private void crawl(String url) {
        PoliceTicker result = new PoliceTicker();
        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("leipzig crawler").timeout(10000).get();
            if (doc != null) {
                result = convertToDataModel(doc);
                result.setUrl(url);
                result.setArticleId(Utils.getArticleId(url));
                logger.info("crawled {}", url);
                debugPrint(result);
                policeTickers.add(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * mapper to map the information of the document into a model
     * 
     * @param path
     *            the url
     * @param doc
     *            the document
     * @return the model with all infromation which are needed
     */
    private PoliceTicker convertToDataModel(Document doc) {
        PoliceTicker dm = new PoliceTicker();
        extractTitle(doc, dm);
        extractArticleAndsnippet(doc, dm);
        extractCopyright(doc, dm);
        extractDatePublished(doc, dm);
        return dm;
    }

    /**
     * prints the details of the detail view
     * 
     * @param dm
     */
    private void debugPrint(PoliceTicker dm) {
        logger.debug("Article: {}", dm.getArticle());
        logger.debug("Copyright: {}", dm.getCopyright());
        logger.debug("Published: {}", dm.getDatePublished());
        logger.debug("Snippet: {}", dm.getSnippet());
        logger.debug("Title: {}", dm.getTitle());
        logger.debug("URL: {}", dm.getUrl());
    }

    /**
     * extracts the title from the document
     * 
     * @param doc
     *            the document with the article
     * @param dm
     *            the datamodel for the information which are needed
     */
    private void extractTitle(Document doc, PoliceTicker dm) {
        String ownText = doc.select("title").first().ownText();
        dm.setTitle(ownText);
    }

    /**
     * extracts the copyright
     * 
     * @param doc
     * @param dm
     */
    private void extractCopyright(Document doc, PoliceTicker dm) {
        String copyrightAndDatePublished = extractCopyrightAndDatePublished(doc);
        dm.setCopyright(copyrightAndDatePublished.split(",")[0]);
    }

    private void extractDatePublished(Document doc, PoliceTicker dm) {
        String copyrightAndDatePublished = extractCopyrightAndDatePublished(doc);

        String date = copyrightAndDatePublished.substring(copyrightAndDatePublished.indexOf(",") + 1).trim();
        DateTimeFormatter fmt = DateTimeFormat.forPattern("dd.MM.YYYY, HH:mm 'Uhr'");

        Date date2 = DateTime.parse(date, fmt).toDateTimeISO().toDate();
        dm.setDatePublished(date2);
    }

    private String extractCopyrightAndDatePublished(Document doc) {
        String result = "";
        for (Element e : doc.select("div.copyright")) {
            // only plain copyright
            if (e.hasText()) {
                result = e.ownText();
            }
        }
        return result;
    }

    private void extractArticleAndsnippet(Document doc, PoliceTicker dm) {
        for (Element e : doc.select("div.ARTIKEL_TEXT")) {
            if (e.hasText()) {
                String article = e.ownText();
                String[] split = article.split("\\s");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < Math.min(20, split.length); i++) {
                    sb.append(split[i]).append(" ");
                }
                String abstractDesc = sb.toString().trim() + "...";
                dm.setArticle(article);
                dm.setSnippet(abstractDesc);
            }
        }
    }
}
