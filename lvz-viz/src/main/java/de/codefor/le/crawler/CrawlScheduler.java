package de.codefor.le.crawler;

import java.util.ArrayList;
import java.util.Iterator;
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

    private boolean crawlMore = true;

    // 1_800_000ms = 30min
    @Scheduled(fixedDelay = 1_800_000)
    public void crawlSchedule() throws ExecutionException, InterruptedException {
        crawlMore = true;
        logger.info("start crawling");
        int i = 1;
        while (crawlMore) {
            List<String> detailPageUrls = crawlMainPage(i++);
            List<PoliceTicker> details = crawlDetailPages(detailPageUrls);

            logger.info("details size {}", details.size());
            int originDetailSize = details.size();

            if (details != null && !details.isEmpty()) {
                Iterator<PoliceTicker> iterator = details.iterator();
                while (iterator.hasNext()) {
                    PoliceTicker next = iterator.next();
                    String title = next.getTitle();
                    logger.info("{}", title);
                    List<PoliceTicker> findByArticle = policeTickerRepository.findByTitle(title);
                    if (findByArticle == null || findByArticle.isEmpty()) {
                        logger.info("article not in index");
                    } else {
                        logger.info("article IS in index");
                        iterator.remove();
                    }
                }
                logger.info("details after cleanup {}", details.size());
                if (details.size() == 0) {
                    // no new articles in it
                    logger.info("currently no new articles");
                    crawlMore = false;
                } else if (details.size() < originDetailSize) {
                    logger.info("no more to crawl; less articles than before current {}; origin {}", details.size(),
                            originDetailSize);
                    crawlMore = false;
                    policeTickerRepository.save(details);
                } else {
                    crawlMore = true;
                    logger.info("more to crawl");
                    policeTickerRepository.save(details);
                }
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
            List<String> locations = ner.getLocations(policeTicker.getArticle(), true);
            for (String string : locations) {
                logger.debug("{}", string);
                Future<List<Nominatim>> nomFutures = nominatimAsker.execute("Leipzig, " + string);

                List<Nominatim> nominatim = nomFutures.get();
                logger.debug("coords: {}", nominatim);
                if (!nominatim.isEmpty()) {
                    for (Nominatim n : nominatim) {
                        if (n.getLat() != null && !n.getLat().isEmpty()) {
                            GeoPoint g = new GeoPoint(Double.valueOf(n.getLat()), Double.valueOf(n.getLon()));
                            logger.debug("{} ", g.toString());
                            policeTicker.setCoords(g);
                            break; // TODO - remove this hacky break;
                        }
                    }
                }
            }
        }
    }
}
