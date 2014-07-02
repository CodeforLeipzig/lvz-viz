package de.codefor.le.crawler;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.common.geo.GeoPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
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

        Stopwatch watch = Stopwatch.createStarted();
        Future<List<String>> execute = crawler.execute(3);
        List<String> detailPageUrls = execute.get();

        watch.stop();
        logger.info("crawling of the mainpages was done in {} ms", watch.elapsed(TimeUnit.MILLISECONDS));
        Future<List<PoliceTicker>> detailFuture = detailCrawler.execute(detailPageUrls);
        List<PoliceTicker> details = detailFuture.get();
        addCoordsToPoliceTickerInformation(details);

        logger.debug("details {}", details);
        if (details != null && !details.isEmpty()) {
            policeTickerRepository.save(details);
        }
    }

    private void addCoordsToPoliceTickerInformation(List<PoliceTicker> details) throws InterruptedException,
            ExecutionException {
        for (PoliceTicker policeTicker : details) {
            List<String> locations = ner.getLocations(policeTicker.getArticle(), true);
            for (String string : locations) {
                logger.debug("{}", string);
                Future<List<Nominatim>> nomFutures = nominatimAsker.execute("Leipzig, " + string);

                List<Nominatim> nominatim = nomFutures.get();
                logger.debug("coords: {}", nominatim);
                if (!nominatim.isEmpty()) {
                    Nominatim firstNominatim = nominatim.get(0);
                    GeoPoint g = new GeoPoint(Double.valueOf(firstNominatim.getLat()), Double.valueOf(firstNominatim
                            .getLon()));
                    logger.debug("{} ", g.toString());
                    policeTicker.setCoords(g);
                    break; // TODO - remove this hacky break;
                }
            }
        }
    }
}
