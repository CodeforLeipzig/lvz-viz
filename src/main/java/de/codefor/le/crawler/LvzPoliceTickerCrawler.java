package de.codefor.le.crawler;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import com.google.common.base.Stopwatch;

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

    protected static final int REQUEST_TIMEOUT = 30000;

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
        final var watch = Stopwatch.createStarted();
        final var url = String.format(LVZ_POLICE_TICKER_PAGE_URL, page);
        logger.debug("Start crawling {}.", url);
        try {
            return new AsyncResult<>(crawlNewsFromPage(url));
        } finally {
            watch.stop();
            logger.debug("Finished crawling page {} in {} ms.", page, watch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    /**
     * @param url the url to crawl
     * @return links of new articles
     */
    private Collection<String> crawlNewsFromPage(final String url) {
        Document doc;
        try {
            doc = Jsoup.connect(url).userAgent(USER_AGENT).timeout(REQUEST_TIMEOUT).get();
        } catch (IOException e) {
            throw new UncheckedIOException("Request for url " + url + " failed.", e);
        }
        final var links = doc.select("a.pdb-teaser3-teaser-breadcrumb-headline-title-link");
        links.addAll(doc.select("a.pdb-bigteaser-item-teaser-breadcrumb-headline-title-link"));
        final var result = extractNewArticleLinks(links);
        if (links.isEmpty()) {
            logger.debug("No links found on current page. This should be the last available page.");
            this.crawlMore = false;
        } else if (result.isEmpty()) {
            logger.debug("No new articles found on current page. {}",
                    crawlAllMainPages ? "Nevertheless, continue crawling on next page." : "Stop crawling for now.");
            this.crawlMore = crawlAllMainPages;
        } else {
            logger.info("{} new articles found on current page.", result.size());
        }
        return result;
    }

    private Collection<String> extractNewArticleLinks(final Elements links) {
        final Collection<String> result = new ArrayList<>(links.size());
        for (final var link : links) {
            final String detailLink = LVZ_BASE_URL + link.attr("href");
            logger.debug("article url: {}", detailLink);
            if (!detailLink.startsWith(LVZ_POLICE_TICKER_BASE_URL)) {
                logger.debug("article not from policeticker - skip it");
                continue;
            } else if (detailLink.matches("(.*)Blitzer(.*)-in-Leipzig(.*)") ) {
                logger.debug("recurring speed control article - skip it");
                continue;
            }
            policeTickerRepository.ifPresentOrElse(repo -> {
                if (!repo.existsById(Utils.generateHashForUrl(detailLink))) {
                    logger.debug("article not stored yet - save it");
                    result.add(detailLink);
                } else {
                    logger.debug("article already stored - skip it");
                }
            }, () -> result.add(detailLink));
        }
        return result;
    }

    public void resetCrawler() {
        this.crawlMore = true;
    }

    public boolean isMoreToCrawl() {
        return crawlMore;
    }

}
