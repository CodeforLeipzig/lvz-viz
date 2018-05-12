package de.codefor.le.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import com.google.common.base.Stopwatch;

import de.codefor.le.model.PoliceTicker;
import de.codefor.le.repositories.PoliceTickerRepository;
import de.codefor.le.utilities.Utils;
import lombok.RequiredArgsConstructor;

/**
 * @author spinner0815
 * @author sepe81
 */
@Component
@RequiredArgsConstructor
public class LvzPoliceTickerCrawler {

    private static final Logger logger = LoggerFactory.getLogger(LvzPoliceTickerCrawler.class);

    protected static final String USER_AGENT = "leipzig crawler";

    protected static final int REQUEST_TIMEOUT = 10000;

    protected static final String LVZ_BASE_URL = "http://www.lvz.de";

    protected static final String LVZ_POLICE_TICKER_BASE_URL = LVZ_BASE_URL + "/Leipzig/Polizeiticker/Polizeiticker-Leipzig";

    protected static final String LVZ_POLICE_TICKER_PAGE_URL = LVZ_POLICE_TICKER_BASE_URL + "/%s#anchor";

    private final Optional<PoliceTickerRepository> policeTickerRepository;

    @Value("${app.crawlAllMainPages}")
    private boolean crawlAllMainPages;

    /** hint for crawling the next site */
    private boolean crawlMore = true;

    @Async
    public Future<Iterable<String>> execute(final int page) {
        final Stopwatch watch = Stopwatch.createStarted();
        final String url = String.format(LVZ_POLICE_TICKER_PAGE_URL, page);
        logger.info("Start crawling {}.", url);
        final List<String> crawledNews = new ArrayList<>();
        try {
            crawlNewsFromPage(crawledNews, url);
        } catch (final IOException e) {
            logger.error(e.toString(), e);
        }
        watch.stop();
        logger.info("Finished crawling page {} in {} ms.", page, watch.elapsed(TimeUnit.MILLISECONDS));
        return new AsyncResult<>(crawledNews);
    }

    /**
     * @param url the url to crawl
     * @throws IOException if there are problems while writing the detail links to a file
     */
    private void crawlNewsFromPage(final List<String> crawledNews, final String url) throws IOException {
        final Document doc = Jsoup.connect(url).userAgent(USER_AGENT).timeout(REQUEST_TIMEOUT).get();
        final Elements links = doc.select("a.pdb-teaser3-teaser-breadcrumb-headline-title-link");
        for (final Element link : links) {
            final String detailLink = LVZ_BASE_URL + link.attr("href");
            logger.debug("article url: {}", detailLink);
            if (!detailLink.startsWith(LVZ_POLICE_TICKER_BASE_URL)) {
                logger.debug("article not from policeticker - skip it");
                continue;
            }
            final String id = Utils.generateHashForUrl(detailLink);
            if (!id.isEmpty()) {
                PoliceTicker article = null;
                if (policeTickerRepository.isPresent()) {
                    article = policeTickerRepository.get().findOne(id);
                }
                if (article == null) {
                    logger.debug("article not stored yet - save it");
                    crawledNews.add(detailLink);
                } else {
                    logger.debug("article already stored - skip it");
                }
            }
        }
        if (links.isEmpty()) {
            logger.info("No links found on current page. This should be the last available page.");
            this.crawlMore = false;
        } else if (crawledNews.isEmpty()) {
            logger.info("No new articles found on current page. "
                    + (crawlAllMainPages ? "Nevertheless, continue crawling on next page." : "Stop crawling for now."));
            this.crawlMore = crawlAllMainPages;
        }
    }

    public void resetCrawler() {
        this.crawlMore = true;
    }

    public boolean isMoreToCrawl() {
        return crawlMore;
    }

}
