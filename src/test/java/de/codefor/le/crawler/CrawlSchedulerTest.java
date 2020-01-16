package de.codefor.le.crawler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.common.geo.GeoPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import de.codefor.le.model.PoliceTicker;

@ActiveProfiles("test")
@SpringBootTest
public class CrawlSchedulerTest {

    private static final String URL = "http://www.lvz.de/Leipzig/Polizeiticker/Polizeiticker-Leipzig/Zwei-80-jaehrige-Damen-in-der-Eythraer-Strasse-ausgeraubt";

    private static final String ARTICLE = "Leipzig. Am Samstagabend hat ein Unbekannter zwei 80-jährige Leipzigerinnen ausgeraubt."
            + "[...]Am Hauseingang der Eythraer Straße 15 %ssei der Dieb dann auf die Damen zugekommen[...]";

    @Autowired
    private CrawlScheduler scheduler;

    private PoliceTicker ticker;

    @BeforeEach
    public void setup() {
        ticker = new PoliceTicker();
        ticker.setUrl(URL);
    }

    @Test
    @Disabled
    void crawl() throws ExecutionException, InterruptedException {
        scheduler.crawl();
    }

    @Test
    void addCoordsToPoliceTickerInformationWithNoSpecificLocation()
            throws ExecutionException, InterruptedException {
        ticker.setArticle(String.format(ARTICLE, ""));
        scheduler.addCoordsToPoliceTickerInformation(Collections.singletonList(ticker));
        assertThat(ticker.getCoords()).isNull();
    }

    @Test
    void addCoordsToPoliceTickerInformationWithSpecificLocationInLeipzig()
            throws ExecutionException, InterruptedException {
        ticker.setArticle(String.format(ARTICLE, "in Kleinzschocher "));
        scheduler.addCoordsToPoliceTickerInformation(Collections.singletonList(ticker));
        assertThat(ticker.getCoords()).isNotNull();

        ticker.setArticle(String.format(ARTICLE, "in Heiterblick und Gohlis-Süd "));
        scheduler.addCoordsToPoliceTickerInformation(Collections.singletonList(ticker));
        assertThat(ticker.getCoords()).isEqualTo(new GeoPoint(51.3606724, 12.359288228937256));
    }
}
