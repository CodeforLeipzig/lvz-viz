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

    private static final Date PUBLISHING_DATE = Date
            .from(LocalDateTime.of(2015, 10, 11, 15, 13).atZone(ZoneId.systemDefault()).toInstant());

    private static final String ARTICLE = "Leipzig. Fast ein halbes Jahr nach einem tödlichen Kranunfall in der Leipziger Innenstadt "
            + "ist die Verantwortung noch immer unklar. Ein technisches Gutachten liege inzwischen vor, "
            + "sagte ein Sprecher der Staatsanwaltschaft in Leipzig. Es sei aber noch unklar, "
            + "ob für das Unglück jemand strafrechtlich verantwortlich gemacht werden könne. "
            + "Noch immer werde das Ermittlungsverfahren gegen unbekannt geführt.";

    private static final String COPYRIGHT = "© Leipziger Verlags- und Druckereigesellschaft mbH & Co. KG";

    private final LvzPoliceTickerDetailViewCrawler crawler = new LvzPoliceTickerDetailViewCrawler();

    @Test
    public void testExecuteForPageWithOffsetZero() throws InterruptedException, ExecutionException {
        final List<String> urls = new ArrayList<>();
        urls.add("http://www.lvz.de/Leipzig/Polizeiticker/Polizeiticker-Leipzig"
                + "/Ermittlungen-nach-toedlichem-Kranunfall-in-Leipzig-City-dauern-an");
        urls.add("http://www.lvz.de/Leipzig/Polizeiticker/Polizeiticker-Leipzig/Betrunkene-rauscht-im-Leipziger-Zentrum-ins-Gleisbett");
        urls.add("http://www.lvz.de/Leipzig/Polizeiticker/Polizeiticker-Leipzig/Taeter-nach-Boellerwurf-in-Asylbewerberwohnung-gefasst");
        urls.add("http://www.lvz.de/Leipzig/Polizeiticker/Polizeiticker-Leipzig"
                + "/Autoanhaenger-mit-Legida-Buehne-in-Leipzig-mit-Molotow-Cocktails-angegriffen");
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
    public void testExtractDate() {
        assertNull(LvzPoliceTickerDetailViewCrawler.extractDate(null));
        assertNull(LvzPoliceTickerDetailViewCrawler.extractDate(""));
        assertNull(LvzPoliceTickerDetailViewCrawler.extractDate(" "));
        assertNull(LvzPoliceTickerDetailViewCrawler.extractDate("2015-10-11"));
        assertEquals(PUBLISHING_DATE, LvzPoliceTickerDetailViewCrawler.extractDate("2015-10-11T15:13:00"));
        assertEquals(PUBLISHING_DATE, LvzPoliceTickerDetailViewCrawler.extractDate("2015-10-11T15:13:00Z"));
        assertEquals(PUBLISHING_DATE, LvzPoliceTickerDetailViewCrawler.extractDate("2015-10-11T15:13:00+02:00"));

    }
}
