package de.codefor.le.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import de.codefor.le.model.PoliceTicker;
import de.codefor.le.repositories.PoliceTickerRepository;
import de.codefor.le.utilities.Utils;

/**
 * TODO should be renamed to LVZPoliceTickerCrawler
 * 
 * @author spinner0815
 */
@Component
public class LVBPoliceTickerCrawler {

    private static final Logger logger = LoggerFactory.getLogger(LVBPoliceTickerCrawler.class);

    private static final String LVZ_BASE_URL = "http://www.lvz-online.de";

    private static final String LVZ_POLICE_BASE_URL = LVZ_BASE_URL
            + "/leipzig/polizeiticker/polizeiticker-leipzig/r-polizeiticker-leipzig-seite-";

    @Autowired
    PoliceTickerRepository policeTickerRepository;

    private boolean crawlMore = true;

    @Async
    public Future<List<String>> execute(int page) {
        final List<String> crawledNews = new ArrayList<>();
        try {
            crawlMore = crawlNewsFromPage(crawledNews, page);
        } catch (final IOException e) {
            logger.error(e.toString(), e);
        }
        return new AsyncResult<List<String>>(crawledNews);
    }

    /**
     * @param page the page which to crawl
     * @return true if all content of the current page is new. Hint for also crawling the next site
     * @throws IOException if there are problems while writing the detail links to a file
     */
    private boolean crawlNewsFromPage(List<String> crawledNews, int page) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder(LVZ_POLICE_BASE_URL);
        stringBuilder.append(page);
        stringBuilder.append(".html");
        // read everytime the file for getting all inserted links: already exists check
        final String url = stringBuilder.toString();
        logger.info("Start crawling page {} at url {}", page, url);
        final Document doc = Jsoup.connect(url).userAgent("leipzig crawler").timeout(10000).get();
        final Elements links = doc.select("a:contains(mehr...)");
        for (final Element link : links) {
            final String detailLink = LVZ_BASE_URL + link.attr("href");
            final String articleId = Utils.getArticleId(detailLink);
            if (!articleId.isEmpty()) {
                final List<PoliceTicker> findByArticleId = policeTickerRepository.findByArticleId(articleId);
                if (findByArticleId == null || findByArticleId.isEmpty()) {
                    logger.debug("article not stored yet: {}", detailLink);
                    crawledNews.add(detailLink);
                } else {
                    logger.debug("article already stored: {}", detailLink);
                }
            }
        }
        if (crawledNews.isEmpty()) {
            logger.info("No new articles found on this page");
        }
        boolean result = true;
        if (links.isEmpty()) {
            logger.info("No links found on this page, this should be the last available page");
            result = false;
        }
        logger.info("Crawled page {}", page);
        return result;
    }

    public void resetCrawler() {
        this.crawlMore = true;
    }

    public boolean isMoreToCrawl() {
        return crawlMore;
    }

}
