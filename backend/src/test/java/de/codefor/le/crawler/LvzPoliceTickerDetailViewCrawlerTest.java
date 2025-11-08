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

class LvzPoliceTickerDetailViewCrawlerTest {

    private static final String BASE_URL = LvzPoliceTickerCrawler.LVZ_BASE_URL + "/lokales/leipzig";

    private static final Date PUBLISHING_DATE = getDate(LocalDateTime.of(2022, 6, 12, 11, 13, 7));

    private static final String ARTICLE = "Leipzig. Eine historische Flüssigbrandbombe hat am Samstag im Leipziger Südwesten einen Polizeieinsatz ausgelöst."
            + " Ein Passant habe den metallischen Gegenstand gegen 16 Uhr in der Nähe der Brückenstraße und des Lauerschen Wegs entdeckt,"
            + " teilte die Polizei am Sonntag mit. Demnach sei anschließend der Kampfmittelbeseitigungsdienst angerückt"
            + " und habe den Fund bestätigt: Der 15 Kilogramm schwere Sprengkörper stellte sich als eine britische Kriegsbombe heraus.";

    private final LvzPoliceTickerDetailViewCrawler crawler = new LvzPoliceTickerDetailViewCrawler();

    private static Date getDate(LocalDateTime localDate) {
        return Date.from(localDate.atZone(ZoneId.of("Europe/Berlin")).toInstant());
    }

    @Test
    void extractArticles() throws InterruptedException, ExecutionException {
        final List<String> urls = new ArrayList<>();
        urls.add(BASE_URL + "/leipzig-passant-findet-brandbombe-bei-der-weissen-elster-IUMQNWJHYTVQ25B22EBFOGDHFE.html");
        urls.add(BASE_URL + "/sie-trugen-keine-maske-junge-frau-greift-personen-im-leipziger-oepnv-an-SIJPHYDMBAXAGKPZN6M2GTNG4Y.html");
        urls.add(BASE_URL + "/leipziger-hauptbahnhof-bundespolizei-zu-pfingsten-im-dauereinsatz-YCHTE7EJJ6FNRYP4GHRQK3LDN4.html ");
        urls.add(BASE_URL + "/ueberfall-in-leipzig-maenner-mit-ffp2-masken-rauben-34-jaehrigen-aus-75IBPAYKCFB5P5JOHSUUIF7ZWI.html");

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
                    assertThat(ticker.getCopyright()).isEqualTo("LVZ");
                });

        assertThat(results).filteredOn(ticker -> ticker.getArticle().contains("FFP2-Masken")).hasSize(1);

        final var softly = new SoftAssertions();
        for (var ticker : results) {
            softly.assertThat(ticker.getDatePublished()).isNotNull();
            softly.assertThat(ticker.getArticle()).isNotEmpty();
        }
        softly.assertAll();
    }

    @Test
    void extractArticleWithPaidContentFromLeipzig() throws InterruptedException, ExecutionException {
        final var result = crawler.execute(
                BASE_URL + "/mordversuch-in-markranstaedt-ich-stech-dich-ab-das-ueberlebst-du-nicht-4LYDGCIHCT6YSUNL7WEGOCNZCU.html").get();
        assertThat(result.getArticle()).isNull();
        assertThat(result.getSnippet()).isEqualTo(
                "Während eines gemütlichen Abends auf dem Sofa soll ein Mann in Markranstädt plötzlich auf seinen "
                        + "Bekannten eingestochen haben, um ihn zu töten. Der rätselhafte Fall kommt nun vor Gericht.");
    }

    @Test
    void extractArticleWithPaidContentFromNordsachsen() throws InterruptedException, ExecutionException {
        final var result = crawler.execute(LvzPoliceTickerCrawler.LVZ_BASE_URL
                + "/lokales/nordsachsen/delitzsch/lok-faengt-zwischen-delitzsch-und-halle-feuer-7IQFKT3TBZPER6364QHNAEDMR4.html").get();
        assertThat(result.getArticle()).isNull();
        assertThat(result.getSnippet()).isEqualTo(
                "Eine Lokomotive ist auf der Bahnstrecke zwischen Delitzsch und Halle/Saale in Brand geraten. "
                        + "Der Bahnverkehr scheint bisher nicht beeinflusst.");
    }

    @Disabled("no active sites with speed controls available")
    @ParameterizedTest
    @CsvSource({
            "/Blitzer-heute-in-Leipzig-Wo-wird-am-Montag-18.-Oktober-2021-geblitzt",
            "/Blitzer-in-Leipzig-Wo-wird-heute-geblitzt-1.-Maerz-2021",
            "/Blitzer-in-Leipzig-Wo-wird-heute-am-12.-Mai-2021-geblitzt"
    })
    void extractArticlesWithSpeedControls(final String path) throws ExecutionException, InterruptedException {
        assertThat(crawler.execute(BASE_URL + path).get())
                .satisfies(ticker -> assertThat(ticker.getTitle()).matches("Hier wird am \\w+ in Leipzig geblitzt"));
    }

    @ParameterizedTest
    @CsvSource({
            "/unfall-im-leipziger-norden-motorrad-von-transporter-erfasst-fahrer-schwer-verletzt-DMCSVDGWNJ3EMPYYQZHGAW42W4.html, 25.05.2022 08:23:25, LVZ",
            "/leipzig-passant-findet-brandbombe-bei-der-weissen-elster-IUMQNWJHYTVQ25B22EBFOGDHFE.html, 12.06.2022 11:13:07, LVZ"
    })
    void extractPublishedDate(final String path,
            @JavaTimeConversionPattern("dd.MM.yyyy HH:mm:ss") final LocalDateTime published, final String copyright)
            throws InterruptedException, ExecutionException {
        assertThat(crawler.execute(BASE_URL + path).get()).isNotNull().satisfies(ticker -> {
            assertThat(ticker.getDatePublished()).isEqualTo(getDate(published));
            assertThat(ticker.getCopyright()).isEqualTo(copyright);
        });
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "2015-10-11", "015-10-11T15:13:00+02:00" })
    void extractDateFails(final String date) {
        assertThat(LvzPoliceTickerDetailViewCrawler.extractDate(date)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = { "2022-06-12T11:13:07", "2022-06-12T11:13:07Z", "2022-06-12T11:13:07+02:00" })
    void extractDate(final String date) {
        assertThat(LvzPoliceTickerDetailViewCrawler.extractDate(date)).isEqualTo(PUBLISHING_DATE);
    }
}
