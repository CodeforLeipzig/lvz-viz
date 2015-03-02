package de.codefor.le.crawler;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.hamcrest.Matchers;
import org.junit.Test;

public class LvzPoliceTickerCrawlerTest {

    private final LvzPoliceTickerCrawler crawler = new LvzPoliceTickerCrawler();

    @Test
    public void testExecuteForPageOneAndZero() throws InterruptedException, ExecutionException {
        final Future<Iterable<String>> future = crawler.execute(1);
        assertNotNull(future);
        final Iterable<String> pageOne = future.get();
        assertNotNull(pageOne);
        final Iterator<String> it = pageOne.iterator();
        assertTrue(it.hasNext());
        final String firstArticleUrl = it.next();
        assertThat(firstArticleUrl, startsWith(LvzPoliceTickerCrawler.LVZ_POLICE_TICKER_BASE_URL));
        assertThat(firstArticleUrl, containsString(LvzPoliceTickerCrawler.REF_TOKEN));
        assertThat(firstArticleUrl, containsString(LvzPoliceTickerCrawler.FILE_ENDING_HTML));

        // page 0 redirects to page 1
        final Iterable<String> pageZero = crawler.execute(0).get();
        assertEquals(pageZero, pageOne);
    }

    @Test
    public void testExecuteForPageMaxInteger() throws InterruptedException, ExecutionException {
        final Iterable<String> result = crawler.execute(Integer.MAX_VALUE).get();
        assertNotNull(result);
        assertThat(result, Matchers.emptyIterableOf(String.class));
    }

}
