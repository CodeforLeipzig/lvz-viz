package de.codefor.le.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.codefor.le.model.PoliceTicker;
import de.codefor.le.repositories.PoliceTickerRepository;

/**
 * 
 * @author spinner0815
 * 
 */
@Component
public class LVBPoliceTickerCrawler extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(LVBPoliceTickerCrawler.class);

    @Autowired
    private PoliceTickerRepository policeTickerRepository;

    private List<String> crawledNews = new ArrayList<>();
    private int maxPages;

    public List<String> getPoliceNewsPages() {
        return crawledNews;
    }

    @Override
    public void run() {
        int i = 1;
        try {
            boolean more = true;
            while (more && i < maxPages) {
                more = crawlPage(i++);
                Thread.sleep(5000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setMaxPages(int maxPages) {
        this.maxPages = maxPages;
    }

    /**
     * 
     * @param page
     *            the page which to crawl
     * @return true if all content of the current page is new. Hint for also carwling the next site
     * @throws IOException
     *             if there are problems while writing the detail links to a file
     */
    private boolean crawlPage(int page) throws IOException {
        boolean result = false;

        // read everytime the file for getting all inserted links: already exists check
        Document doc = null;
        doc = Jsoup.connect(
                "http://www.lvz-online.de/leipzig/polizeiticker/polizeiticker-leipzig/r-polizeiticker-leipzig-seite-"
                        + page + ".html").get();
        for (Element e : doc.select("a:contains(mehr...)")) {
            String detailLink = "http://www.lvz-online.de" + e.attr("href");
            logger.debug("link to detail page {}", detailLink);

            Iterator<PoliceTicker> search = policeTickerRepository.search(QueryBuilders.boolQuery().must(
                    QueryBuilders.termQuery("url", detailLink))).iterator();
            logger.info("{}", detailLink);
            while (search.hasNext()) {
                logger.info("allready crawled {}", detailLink.toString());
                result = false;
                return result;// FIXME - ugly return at this point.
            }
            crawledNews.add(detailLink);
        }

        if (policeTickerRepository.findByUrlIn(crawledNews).isEmpty()
                && crawledNews.size() == doc.select("a:contains(mehr...)").size()) {
            logger.debug("there is more to crawl!");
            result = true;
        }

        return result;
    }
}
