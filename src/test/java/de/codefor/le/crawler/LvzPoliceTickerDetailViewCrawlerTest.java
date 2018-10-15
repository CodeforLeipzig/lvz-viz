package de.codefor.le.crawler;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Test;

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
    public void extractArticles() throws InterruptedException, ExecutionException {
        final List<String> urls = new ArrayList<>();
        urls.add(BASE_URL + "/Ermittlungen-nach-toedlichem-Kranunfall-in-Leipzig-City-dauern-an");
        urls.add(BASE_URL + "/Betrunkene-rauscht-im-Leipziger-Zentrum-ins-Gleisbett");
        urls.add(BASE_URL + "/Taeter-nach-Boellerwurf-in-Asylbewerberwohnung-gefasst");
        urls.add(BASE_URL + "/Schwerer-Unfall-Strassenbahn-erfasst-Radfahrerin-in-Leipzig");
        urls.add("http://www.lvz.de/Specials/Themenspecials/Legida-und-Proteste"
                + "/Pegida/Nach-Pegida-Auseinandersetzung-auch-am-Leipziger-Hauptbahnhof");

        final Future<Iterable<PoliceTicker>> future = crawler.execute(urls);
        assertNotNull(future);
        final Iterable<PoliceTicker> results = future.get();
        assertNotNull(results);

        final Iterator<PoliceTicker> it = results.iterator();
        PoliceTicker ticker = it.next();
        assertEquals(PUBLISHING_DATE, ticker.getDatePublished());
        assertThat(ticker.getArticle(), startsWith(ARTICLE));
        assertEquals(COPYRIGHT, ticker.getCopyright());

        while (it.hasNext()) {
            ticker = it.next();
            assertNotNull(ticker.getDatePublished());
            assertNotNull(ticker.getArticle());
            assertEquals(COPYRIGHT, ticker.getCopyright());
        }

        assertThat(ticker.getArticle(), containsString("Identitätsfeststellung"));
    }

    @Test
    public void extractPublishedDate() throws InterruptedException, ExecutionException {
        final List<String> urls = new ArrayList<>();
        urls.add(BASE_URL + "/Motorradfahrer-bei-Unfall-in-Leipzig-schwer-verletzt");
        urls.add(BASE_URL + "/Krawalle-am-Leipziger-Amtsgericht-191-Verfahren-eingestellt");
        final Future<Iterable<PoliceTicker>> future = crawler.execute(urls);
        assertNotNull(future);
        final Iterable<PoliceTicker> results = future.get();
        assertNotNull(results);

        final Iterator<PoliceTicker> it = results.iterator();
        PoliceTicker ticker = it.next();

        assertEquals(getDate(LocalDateTime.of(2016, 3, 30, 10, 35, 36)), ticker.getDatePublished());

        ticker = it.next();
        assertEquals(getDate(LocalDateTime.of(2016, 5, 7, 10, 00)), ticker.getDatePublished());
    }

    @Test
    public void extractDate() {
        assertNull(LvzPoliceTickerDetailViewCrawler.extractDate(null));
        assertNull(LvzPoliceTickerDetailViewCrawler.extractDate(""));
        assertNull(LvzPoliceTickerDetailViewCrawler.extractDate(" "));
        assertNull(LvzPoliceTickerDetailViewCrawler.extractDate("2015-10-11"));
        assertEquals(PUBLISHING_DATE, LvzPoliceTickerDetailViewCrawler.extractDate("2015-10-11T15:13:00"));
        assertEquals(PUBLISHING_DATE, LvzPoliceTickerDetailViewCrawler.extractDate("2015-10-11T15:13:00Z"));
        assertEquals(PUBLISHING_DATE, LvzPoliceTickerDetailViewCrawler.extractDate("2015-10-11T15:13:00+02:00"));

    }
}
