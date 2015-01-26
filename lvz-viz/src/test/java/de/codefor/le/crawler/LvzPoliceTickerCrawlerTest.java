package de.codefor.le.crawler;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.hamcrest.Matchers;
import org.junit.Test;

public class LvzPoliceTickerCrawlerTest {

    private final LVBPoliceTickerCrawler crawler = new LVBPoliceTickerCrawler();

    @Test
    public void testExecuteForPageOneAndZero() throws InterruptedException, ExecutionException {
        final Future<List<String>> future = crawler.execute(1);
        assertNotNull(future);
        final List<String> pageOne = future.get();
        assertNotNull(pageOne);
        final Iterator<String> it = pageOne.iterator();
        assertTrue(it.hasNext());
        final String firstArticleUrl = it.next();
        assertThat(firstArticleUrl, startsWith(LVBPoliceTickerCrawler.LVZ_POLICE_TICKER_BASE_URL));
        assertThat(firstArticleUrl, containsString(LVBPoliceTickerCrawler.REF_TOKEN));
        assertThat(firstArticleUrl, containsString(LVBPoliceTickerCrawler.FILE_ENDING_HTML));

        // page 0 redirects to page 1
        final List<String> pageZero = crawler.execute(0).get();
        assertEquals(pageZero, pageOne);
    }

    @Test
    public void testExecuteForPageMaxInteger() throws InterruptedException, ExecutionException {
        final List<String> result = crawler.execute(Integer.MAX_VALUE).get();
        assertNotNull(result);
        assertThat(result, Matchers.emptyCollectionOf(String.class));
    }

}
