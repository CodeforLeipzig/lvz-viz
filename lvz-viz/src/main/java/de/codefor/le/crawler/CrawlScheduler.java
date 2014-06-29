package de.codefor.le.crawler;

import java.util.List;
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
//    @Scheduled(fixedDelay = 1_800_000)
    public void crawlSchedule() throws InterruptedException {
        logger.info("pause");
        // Thread.sleep(5000);
        logger.info("Test");

        Stopwatch watch = Stopwatch.createStarted();
        crawler.setMaxPages(1);
        crawler.start();
        crawler.join();
        watch.stop();
        logger.debug("crawl was done in {} ms", watch.elapsed(TimeUnit.MILLISECONDS));
        List<String> policeNewsPages = crawler.getPoliceNewsPages();

        detailCrawler.setDetailUrls(policeNewsPages);
        detailCrawler.start();
        detailCrawler.join();
        List<PoliceTicker> details = detailCrawler.getDetails();
        for (PoliceTicker policeTicker : details) {
            List<String> locations = ner.getLocations(policeTicker.getArticle(), true);
            for (String string : locations) {
                logger.debug("{}", string);
                nominatimAsker.setAdress("Leipzig, " + string);
                nominatimAsker.run();
                nominatimAsker.join();
                List<Nominatim> nominatim = nominatimAsker.getNominatim();
                logger.debug("coords: {}", nominatim);
                if (!nominatim.isEmpty()) {
                    Nominatim firstNominatim = nominatim.get(0);
                    GeoPoint g = new GeoPoint(Double.valueOf(firstNominatim.getLat()), Double.valueOf(firstNominatim
                            .getLon()));
                    logger.debug("{} ", g.toString());
                    policeTicker.setCoords(g);
                }
            }
        }
        logger.debug("details {}", details);
        if (details != null && !details.isEmpty()) {
            policeTickerRepository.save(details);
        }
    }
}
