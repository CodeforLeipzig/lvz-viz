package de.codefor.le.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private NER ner;
    @Autowired
    NominatimAsker nominatimAsker;

    // 1_800_000ms = 30min
    @Scheduled(fixedDelay = 1_800_000)
    public void crawlSchedule() throws ExecutionException, InterruptedException {
        logger.info("start crawling");
        int i = 1;
        while (crawler.isMoreToCrawl()) {
            List<String> detailPageUrls = crawlMainPage(i++);
            List<PoliceTicker> details = crawlDetailPages(detailPageUrls);
            if (!details.isEmpty()) {
                policeTickerRepository.save(details);
            }
        }
    }

    private List<String> crawlMainPage(int i) throws InterruptedException, ExecutionException {
        List<String> result = new ArrayList<>();
        logger.info("Start crawling page {}", i);
        Stopwatch watch = Stopwatch.createStarted();
        Future<List<String>> execute = crawler.execute(i++);
        result = execute.get();
        watch.stop();
        logger.debug("crawling of the mainpages was done in {} ms", watch.elapsed(TimeUnit.MILLISECONDS));
        return result;
    }

    private List<PoliceTicker> crawlDetailPages(List<String> detailPageUrls) throws InterruptedException,
            ExecutionException {
        Future<List<PoliceTicker>> detailFuture = detailCrawler.execute(detailPageUrls);
        List<PoliceTicker> details = detailFuture.get();
        addCoordsToPoliceTickerInformation(details);
        return details;
    }

    private void addCoordsToPoliceTickerInformation(List<PoliceTicker> details) throws InterruptedException,
            ExecutionException {
        for (PoliceTicker policeTicker : details) {
            boolean coordsFound = false;
            List<String> locations = ner.getLocations(policeTicker.getArticle(), true);
            // TODO - replace to bulk threading (not every page in one thread)
            for (String string : locations) {
                logger.debug("{}", string);
                Future<List<Nominatim>> nomFutures = nominatimAsker.execute("Leipzig, " + string);

                List<Nominatim> nominatim = nomFutures.get();
                logger.debug("{} coords: {}", policeTicker.getUrl(), nominatim);
                if (!nominatim.isEmpty()) {
                    for (Nominatim n : nominatim) {
                        if (n.getLat() != null && !n.getLat().isEmpty()) {
                            GeoPoint g = new GeoPoint(Double.valueOf(n.getLat()), Double.valueOf(n.getLon()));

                            logger.debug("geoPoint {} ", g.toString());
                            policeTicker.setCoords(g);
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
}
