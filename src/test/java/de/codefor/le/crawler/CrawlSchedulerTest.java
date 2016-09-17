package de.codefor.le.crawler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import de.codefor.le.LvzViz;
import de.codefor.le.model.PoliceTicker;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CrawlSchedulerTest {

    private static final String URL = "http://www.lvz.de/Leipzig/Polizeiticker/Polizeiticker-Leipzig/Zwei-80-jaehrige-Damen-in-der-Eythraer-Strasse-ausgeraubt";

    private static final String ARTICLE = "Leipzig. Am Samstagabend hat ein Unbekannter zwei 80-jährige Leipzigerinnen ausgeraubt."
            + "[...]Am Hauseingang der Eythraer Straße 15 %ssei der Dieb dann auf die Damen zugekommen[...]";

    @Autowired
    private CrawlScheduler scheduler;

    private PoliceTicker ticker;

    @Before
    public void setup() {
        ticker = new PoliceTicker();
        ticker.setUrl(URL);
    }

    @Test
    @Ignore
    public void crawlSchedule() throws ExecutionException, InterruptedException {
        scheduler.crawlSchedule();
    }

    @Test
    public void addCoordsToPoliceTickerInformationWithNoSpecificLocation() throws ExecutionException, InterruptedException {
        ticker.setArticle(String.format(ARTICLE, ""));
        scheduler.addCoordsToPoliceTickerInformation(Collections.singletonList(ticker));
        assertNull(ticker.getCoords());
    }

    @Test
    public void addCoordsToPoliceTickerInformationWithSpecificLocationInLeipzig() throws ExecutionException, InterruptedException {
        ticker.setArticle(String.format(ARTICLE, "in Kleinzschocher "));
        scheduler.addCoordsToPoliceTickerInformation(Collections.singletonList(ticker));
        assertNotNull(ticker.getCoords());

        ticker.setArticle(String.format(ARTICLE, "in Heiterblick und Gohlis-Süd "));
        scheduler.addCoordsToPoliceTickerInformation(Collections.singletonList(ticker));
        assertNotNull(ticker.getCoords());
    }
}
