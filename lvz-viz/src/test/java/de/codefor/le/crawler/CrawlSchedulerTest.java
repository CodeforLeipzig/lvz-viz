package de.codefor.le.crawler;

import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.codefor.le.LvzViz;
import de.codefor.le.model.PoliceTicker;

@Ignore("spring boot integration test needs more fine tuning")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = LvzViz.class)
public class CrawlSchedulerTest {

    private final CrawlScheduler scheduler = new CrawlScheduler();

    @Test
    public void crawlSchedule() throws ExecutionException, InterruptedException {
        scheduler.crawlSchedule();
    }

    @Test
    public void addCoordsToPoliceTickerInformation() throws ExecutionException, InterruptedException {
        final PoliceTicker ticker = new PoliceTicker();
        ticker.setArticle("Leipzig. Am Samstagabend hat ein Unbekannter zwei 80-jährige Leipzigerinnen ausgeraubt."
                + "[...]Am Hauseingang der Eythraer Straße 15 sei der Dieb dann auf die Damen zugekommen[...]");
        ticker.setUrl("http://www.lvz-online.de/leipzig/polizeiticker/polizeiticker-leipzig/zwei-80-jaehrige-damen-in-der-eythraer-strasse-ausgeraubt/r-polizeiticker-leipzig-a-4819.html");
        scheduler.addCoordsToPoliceTickerInformation(Collections.singletonList(ticker));
        assertNotNull(ticker.getCoords());
    }
}
