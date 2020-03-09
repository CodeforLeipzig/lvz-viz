package de.codefor.le.crawler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.JavaTimeConversionPattern;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import de.codefor.le.model.PoliceTicker;

public class LvzPoliceTickerDetailViewCrawlerTest {

    private static final String BASE_URL = LvzPoliceTickerCrawler.LVZ_POLICE_TICKER_BASE_URL;

    private static final Date PUBLISHING_DATE = getDate(LocalDateTime.of(2015, 10, 11, 15, 13));

    private static final String ARTICLE = "Fast ein halbes Jahr nach einem tödlichen Kranunfall in der Leipziger Innenstadt "
            + "ist die Verantwortung noch immer unklar. Ein technisches Gutachten liege inzwischen vor, "
            + "sagte ein Sprecher der Staatsanwaltschaft in Leipzig. Es sei aber noch unklar, "
            + "ob für das Unglück jemand strafrechtlich verantwortlich gemacht werden könne. "
            + "Noch immer werde das Ermittlungsverfahren gegen unbekannt geführt.";

    private static final String COPYRIGHT = "© Leipziger Verlags- und Druckereigesellschaft mbH & Co. KG";

    private final LvzPoliceTickerDetailViewCrawler crawler = new LvzPoliceTickerDetailViewCrawler();

    private static Date getDate(LocalDateTime localDate) {
        return Date.from(localDate.atZone(ZoneId.of("Europe/Berlin")).toInstant());
    }

    @Test
    void extractArticles() throws InterruptedException, ExecutionException {
        final List<String> urls = new ArrayList<>();
        urls.add(BASE_URL + "/Ermittlungen-nach-toedlichem-Kranunfall-in-Leipzig-City-dauern-an");
        urls.add(BASE_URL + "/Betrunkene-rauscht-im-Leipziger-Zentrum-ins-Gleisbett");
        urls.add(BASE_URL + "/Taeter-nach-Boellerwurf-in-Asylbewerberwohnung-gefasst");
        urls.add(BASE_URL + "/Schwerer-Unfall-Strassenbahn-erfasst-Radfahrerin-in-Leipzig");
        urls.add("http://www.lvz.de/Specials/Themenspecials/Legida-und-Proteste"
                + "/Pegida/Nach-Pegida-Auseinandersetzung-auch-am-Leipziger-Hauptbahnhof");

        final List<PoliceTicker> results = new ArrayList<>(urls.size());
        for (var url : urls) {
            final var future = crawler.execute(url);
            assertThat(future).isNotNull().isNotCancelled();
            results.add(future.get());
        }
        assertThat(results).isNotNull().hasSize(urls.size());

        assertThat(results).filteredOn(ticker -> ticker.getArticle().startsWith(ARTICLE)).hasSize(1).first()
                .satisfies(ticker -> {
                    assertThat(ticker.getDatePublished()).isEqualTo(PUBLISHING_DATE);
                    assertThat(ticker.getCopyright()).isEqualTo(COPYRIGHT);
                });

        assertThat(results).filteredOn(ticker -> ticker.getArticle().contains("Identitätsfeststellung")).hasSize(1);

        final var softly = new SoftAssertions();
        for (var ticker : results) {
            softly.assertThat(ticker.getDatePublished()).isNotNull();
            softly.assertThat(ticker.getArticle()).isNotEmpty();
            softly.assertThat(ticker.getCopyright()).isEqualTo(COPYRIGHT);
        }
        softly.assertAll();
    }

    @Disabled("until new paid content is available")
    @Test
    void extractArticleWithPaidContent() throws InterruptedException, ExecutionException {
        final var result = crawler.execute(BASE_URL + "/Mordversuch-in-Markranstaedt-Ich-stech-dich-ab-das-ueberlebst-du-nicht").get();
        assertThat(result.getArticle()).isEqualTo("Sie saßen auf dem Sofa und hörten Musik, dann zückte einer von ihnen ganz plötzlich ein Messer:"
                + " Nach einer vollkommen unerklärlichen Bluttat in Markr...");
    }

    @ParameterizedTest
    @CsvSource({ "/Motorradfahrer-bei-Unfall-in-Leipzig-schwer-verletzt, 30.03.2016 10:35:36",
            "/Krawalle-am-Leipziger-Amtsgericht-191-Verfahren-eingestellt, 07.05.2016 10:00:00" })
    void extractPublishedDate(final String path,
            @JavaTimeConversionPattern("dd.MM.yyyy HH:mm:ss") final LocalDateTime published)
            throws InterruptedException, ExecutionException {
        assertThat(crawler.execute(BASE_URL + path).get()).isNotNull().satisfies(ticker -> {
            assertThat(ticker.getDatePublished()).isEqualTo(getDate(published));
            assertThat(ticker.getCopyright()).isEqualTo(COPYRIGHT);
        });
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "2015-10-11", "015-10-11T15:13:00+02:00" })
    void extractDateFails(final String date) {
        assertThat(LvzPoliceTickerDetailViewCrawler.extractDate(date)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = { "2015-10-11T15:13:00", "2015-10-11T15:13:00Z", "2015-10-11T15:13:00+02:00" })
    void extractDate(final String date) {
        assertThat(LvzPoliceTickerDetailViewCrawler.extractDate(date)).isEqualTo(PUBLISHING_DATE);
    }
}
