package de.codefor.le.crawler;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Test;

public class LvzPoliceTickerCrawlerTest {

    private final LvzPoliceTickerCrawler crawler = new LvzPoliceTickerCrawler(Optional.empty());

    @Test
    public void testExecuteForPageZeroAndOne() throws InterruptedException, ExecutionException {
        final Future<Iterable<String>> future = crawler.execute(0);
        assertNotNull(future);
        final Iterable<String> pageOne = future.get();
        assertNotNull(pageOne);
        final String firstArticleUrl = pageOne.iterator().next();
        assertThat(firstArticleUrl, startsWith(LvzPoliceTickerCrawler.LVZ_POLICE_TICKER_BASE_URL));

        assertEquals(firstArticleUrl, crawler.execute(1).get().iterator().next());
    }

    @Test
    public void testExecuteForPageMaxInteger() throws InterruptedException, ExecutionException {
        final Iterable<String> result = crawler.execute(Integer.MAX_VALUE).get();
        assertNotNull(result);
    }

}
