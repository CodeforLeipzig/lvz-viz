package de.codefor.le.crawler;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.common.geo.GeoPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.base.Stopwatch;

import de.codefor.le.crawler.model.Nominatim;
import de.codefor.le.model.PoliceTicker;
import de.codefor.le.ner.NER;
import de.codefor.le.repositories.PoliceTickerRepository;

@Component
public class CrawlScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CrawlScheduler.class);

    @Autowired
    private PoliceTickerRepository policeTickerRepository;

    @Autowired
    private LVBPoliceTickerCrawler crawler;

    @Autowired
    private LVBPoliceTickerDetailViewCrawler detailCrawler;

    @Autowired(required = false)
    private NER ner;

    @Autowired
    NominatimAsker nominatimAsker;

    // 1_800_000ms = 30min
    @Scheduled(fixedDelay = 1_800_000)
    public void crawlSchedule() throws ExecutionException, InterruptedException {
        logger.info("Start crawling police ticker");
        int i = 1;
        while (crawler.isMoreToCrawl()) {
            final List<String> detailPageUrls = crawlMainPage(i++);
            if (!detailPageUrls.isEmpty()) {
                final List<PoliceTicker> details = crawlDetailPages(detailPageUrls);
                if (ner != null) {
                    addCoordsToPoliceTickerInformation(details);
                }
                if (!details.isEmpty()) {
                    policeTickerRepository.save(details);
                }
            }
        }
        // else the crawler will not start again after the delay
        crawler.resetCrawler();
    }

    private List<String> crawlMainPage(final int i) throws InterruptedException, ExecutionException {
        final Stopwatch watch = Stopwatch.createStarted();
        final Future<List<String>> mainFuture = crawler.execute(i);
        final List<String> result = mainFuture.get();
        watch.stop();
        logger.info("Crawling of page {} was done in {} ms", i, watch.elapsed(TimeUnit.MILLISECONDS));
        return result;
    }

    private List<PoliceTicker> crawlDetailPages(final List<String> detailPageUrls) throws InterruptedException,
            ExecutionException {
        final Stopwatch watch = Stopwatch.createStarted();
        final Future<List<PoliceTicker>> detailFuture = detailCrawler.execute(detailPageUrls);
        final List<PoliceTicker> details = detailFuture.get();
        watch.stop();
        logger.info("Crawling of detail pages was done in {} ms", watch.elapsed(TimeUnit.MILLISECONDS));
        return details;
    }

    void addCoordsToPoliceTickerInformation(final List<PoliceTicker> articles) throws InterruptedException,
    ExecutionException {
        logger.debug("addCoordsToPoliceTickerInformation for {} articles", articles.size());
        for (final PoliceTicker policeTicker : articles) {
            boolean coordsFound = false;
            final List<String> locations = ner.getLocations(policeTicker.getArticle(), true);
            logger.debug("{} locations found: {}", locations.size(), locations);
            // TODO - replace to bulk threading (not every page in one thread)
            for (final String location : locations) {
                logger.debug("search {} in nominatim", location);
                final Future<List<Nominatim>> nomFutures = nominatimAsker.execute("Leipzig, " + location);
                final List<Nominatim> nominatim = nomFutures.get();
                logger.debug("{} coords: {}", policeTicker.getUrl(), nominatim);
                if (!nominatim.isEmpty()) {
                    for (final Nominatim n : nominatim) {
                        if (setCoordsIfValid(policeTicker, n)) {
                            coordsFound = true;
                            break; // TODO - remove this hacky break;
                        }
                    }
                }
                // TODO - better loop
                if (coordsFound) {
                    break;
                }
            }
        }
    }

    /**
     * Set coordinates from nominatim to policeTicker if valid
     *
     * @param policeTicker PoliceTicker
     * @param nominatim Nominatim
     * @return true if nominatim contains valid coordinates
     */
    private boolean setCoordsIfValid(final PoliceTicker policeTicker, final Nominatim nominatim) {
        final String lat = nominatim.getLat();
        final String lon = nominatim.getLon();
        if (NumberUtils.isNumber(lat) && NumberUtils.isNumber(lon)) {
            final GeoPoint g = new GeoPoint(Double.valueOf(lat), Double.valueOf(lon));
            logger.debug("set geoPoint {} to article", g);
            policeTicker.setCoords(g);
            return true;
        } else {
            logger.warn("latitude {} and longitude {} must be non-empty numeric", lat, lon);
        }
        return false;
    }
}
