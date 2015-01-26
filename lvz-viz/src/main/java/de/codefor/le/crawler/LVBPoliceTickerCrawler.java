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

    protected static final String USER_AGENT = "leipzig crawler";

    protected static final String FILE_ENDING_HTML = ".html";

    protected static final String LVZ_BASE_URL = "http://www.lvz-online.de";

    protected static final String LVZ_POLICE_TICKER_BASE_URL = LVZ_BASE_URL
            + "/leipzig/polizeiticker/polizeiticker-leipzig";

    protected static final String REF_TOKEN = "r-polizeiticker-leipzig";

    protected static final String LVZ_POLICE_TICKER_PAGE_URL = LVZ_POLICE_TICKER_BASE_URL + "/" + REF_TOKEN + "-seite-";

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
        final String url = generateUrl(page);
        logger.info("Start crawling page {} at url {}", page, url);
        final Document doc = Jsoup.connect(url).userAgent(USER_AGENT).timeout(10000).get();
        final Elements links = doc.select("a:contains(mehr...)");
        for (final Element link : links) {
            final String detailLink = LVZ_BASE_URL + link.attr("href");
            final String articleId = Utils.getArticleId(detailLink);
            if (!articleId.isEmpty()) {
                List<PoliceTicker> articles = null;
                if (policeTickerRepository != null) {
                    articles = policeTickerRepository.findByArticleId(articleId);
                }
                if (articles == null || articles.isEmpty()) {
                    logger.debug("article not stored yet: {}", detailLink);
                    crawledNews.add(detailLink);
                } else {
                    logger.debug("article already stored: {}", detailLink);
                }
            }
        }
        boolean result = true;
        if (crawledNews.isEmpty()) {
            logger.info("No new articles found on this page");
            result = false;
        }
        if (links.isEmpty()) {
            logger.info("No links found on this page, this should be the last available page");
            result = false;
        }
        logger.info("Crawled page {}", page);
        return result;
    }

    private String generateUrl(int page) {
        final StringBuilder sb = new StringBuilder(LVZ_POLICE_TICKER_PAGE_URL);
        sb.append(page);
        sb.append(FILE_ENDING_HTML);
        return sb.toString();
    }

    public void resetCrawler() {
        this.crawlMore = true;
    }

    public boolean isMoreToCrawl() {
        return crawlMore;
    }

}
