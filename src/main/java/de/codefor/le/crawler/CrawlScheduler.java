package de.codefor.le.crawler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.Doubles;

import org.elasticsearch.common.geo.GeoPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.codefor.le.crawler.model.Nominatim;
import de.codefor.le.model.PoliceTicker;
import de.codefor.le.ner.NER;
import de.codefor.le.repositories.PoliceTickerRepository;
import lombok.RequiredArgsConstructor;

@Component
@Profile("crawl")
@RequiredArgsConstructor
public class CrawlScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CrawlScheduler.class);

    private final PoliceTickerRepository policeTickerRepository;

    private final LvzPoliceTickerCrawler crawler;

    private final LvzPoliceTickerDetailViewCrawler detailCrawler;

    private final NER ner;

    private final NominatimAsker nominatimAsker;

    // 1_800_000ms = 30min
    @Scheduled(fixedDelay = 1_800_000, initialDelay = 1000)
    public void crawlSchedule() throws ExecutionException, InterruptedException {
        logger.info("Start crawling police ticker.");
        int page = 1;
        while (crawler.isMoreToCrawl()) {
            final Iterable<String> detailPageUrls = crawlMainPage(page++);
            if (detailPageUrls.iterator().hasNext()) {
                final Iterable<PoliceTicker> details = crawlDetailPages(detailPageUrls);
                if (ner != null) {
                    addCoordsToPoliceTickerInformation(details);
                }
                if (details.iterator().hasNext()) {
                    policeTickerRepository.save(details);
                }
            }
        }
        logger.info("Finished crawling police ticker.");
        // else the crawler will not start again after the delay
        crawler.resetCrawler();
    }

    private Iterable<String> crawlMainPage(final int page) throws InterruptedException, ExecutionException {
        final Future<Iterable<String>> mainFuture = crawler.execute(page);
        final Iterable<String> result = mainFuture.get();
        return result;
    }

    private Iterable<PoliceTicker> crawlDetailPages(final Iterable<String> detailPageUrls) throws InterruptedException, ExecutionException {
        final Future<Iterable<PoliceTicker>> detailFuture = detailCrawler.execute(detailPageUrls);
        final Iterable<PoliceTicker> details = detailFuture.get();
        return details;
    }

    @VisibleForTesting
    void addCoordsToPoliceTickerInformation(final Iterable<PoliceTicker> articles) throws InterruptedException, ExecutionException {
        logger.debug("addCoordsToPoliceTickerInformation for various articles");
        for (final PoliceTicker policeTicker : articles) {
            logger.debug("process article {}", policeTicker.getUrl());
            for (final String location : ner.getLocations(policeTicker.getArticle(), true)) {
                logger.debug("search '{}' in nominatim", location);
                nominatimAsker.execute(NominatimAsker.NOMINATIM_SEARCH_CITY_PREFIX + location).get().stream()
                        .filter(CrawlScheduler::hasValidGeoCoords).map(CrawlScheduler::createGeoPoint).findFirst()
                        .ifPresent(g -> {
                            logger.debug("set GeoPoint {} to article", g);
                            policeTicker.setCoords(g);
                        });
                if (policeTicker.getCoords() != null) {
                    break;
                }
            }
        }
    }

    private static boolean hasValidGeoCoords(final Nominatim nominatim) {
        final String lat = nominatim.getLat();
        final String lon = nominatim.getLon();
        if (Doubles.tryParse(lat) != null && Doubles.tryParse(lon) != null) {
            return true;
        }
        logger.warn("latitude {} and longitude {} must be non-empty numeric", lat, lon);
        return false;
    }

    private static GeoPoint createGeoPoint(final Nominatim nominatim) {
        return new GeoPoint(Double.parseDouble(nominatim.getLat()), Double.parseDouble(nominatim.getLon()));
    }
}
