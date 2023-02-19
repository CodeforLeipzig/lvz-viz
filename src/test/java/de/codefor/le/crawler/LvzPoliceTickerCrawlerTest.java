package de.codefor.le.crawler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

class LvzPoliceTickerCrawlerTest {

    private final LvzPoliceTickerCrawler crawler = new LvzPoliceTickerCrawler(Optional.empty());

    @Test
    void testExecute() throws ExecutionException, InterruptedException {
        final var future = crawler.execute();
        assertThat(future).isNotNull().isNotCancelled();
        final var articleUrls = future.get();
        assertThat(articleUrls).isNotEmpty().allSatisfy(article -> {
            assertThat(article).startsWith(LvzPoliceTickerCrawler.LVZ_BASE_URL);
            assertThat(article).doesNotContain("Blitzer-in-Leipzig");
        });
    }

}
