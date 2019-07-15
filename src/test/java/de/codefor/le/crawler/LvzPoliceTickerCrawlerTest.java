package de.codefor.le.crawler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

public class LvzPoliceTickerCrawlerTest {

    private final LvzPoliceTickerCrawler crawler = new LvzPoliceTickerCrawler(Optional.empty());

    @Test
    public void testExecuteForPageZeroAndOne() throws InterruptedException, ExecutionException {
        final Future<Iterable<String>> future = crawler.execute(0);
        assertThat(future).isNotNull();
        final Iterable<String> pageOne = future.get();
        assertThat(pageOne).isNotNull();
        final String firstArticleUrl = pageOne.iterator().next();
        assertThat(firstArticleUrl).startsWith(LvzPoliceTickerCrawler.LVZ_POLICE_TICKER_BASE_URL);

        assertThat(crawler.execute(1).get().iterator().next()).isEqualTo(firstArticleUrl);
    }

    @Test
    public void testExecuteForPageMaxInteger() throws InterruptedException, ExecutionException {
        final Iterable<String> result = crawler.execute(Integer.MAX_VALUE).get();
        assertThat(result).isNotNull();
        final Iterator<String> it = result.iterator();
        // in case of bigteaser-item we expect ONE result, otherwise ZERO
        if (it.hasNext()) {
            assertThat(it.next()).isNotNull();
            assertThat(it).isExhausted();
        }
    }

}
