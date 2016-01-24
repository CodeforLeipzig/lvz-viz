package de.codefor.le.crawler;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.hamcrest.Matchers;
import org.junit.Test;

public class LvzPoliceTickerCrawlerTest {

    private static final String OLDEST_URL = "http://www.lvz.de/Leipzig/Polizeiticker/Polizeiticker-Leipzig/Anbau-von-Hanf-in-Stoetteritzer-Kleingarten-entdeckt";

    private final LvzPoliceTickerCrawler crawler = new LvzPoliceTickerCrawler();

    @Test
    public void testExecuteForPageWithOffsetZero() throws InterruptedException, ExecutionException {
        final Future<? extends Iterable<String>> future = crawler.execute(0);
        assertNotNull(future);
        final Iterable<String> pageOne = future.get();
        assertNotNull(pageOne);
        final Iterator<String> it = pageOne.iterator();
        assertTrue(it.hasNext());
        final String firstArticleUrl = it.next();
        assertThat(firstArticleUrl, startsWith(LvzPoliceTickerCrawler.LVZ_POLICE_TICKER_BASE_URL));
    }

    @Test
    public void testExecuteForPageWithOffset6538() throws InterruptedException, ExecutionException {
        final Iterable<String> result = crawler.execute(6538).get();
        assertNotNull(result);
        assertThat(result, Matchers.hasItem(OLDEST_URL));
    }

    @Test
    public void testExecuteForPageMaxInteger() throws InterruptedException, ExecutionException {
        final Iterable<String> result = crawler.execute(Integer.MAX_VALUE).get();
        assertNotNull(result);
        assertThat(result, Matchers.emptyIterableOf(String.class));
    }

}
