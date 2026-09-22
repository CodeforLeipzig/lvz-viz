package de.codefor.le.crawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.scheduling.annotation.AsyncResult;

import de.codefor.le.model.PoliceTicker;
import de.codefor.le.repositories.PoliceTickerRepository;

class CrawlSchedulerDetailPagesTest {

    private static final String BLOCKED_URL = LvzPoliceTickerCrawler.LVZ_BASE_URL + "/blocked.html";

    private static final String VALID_URL = LvzPoliceTickerCrawler.LVZ_BASE_URL + "/valid.html";

    private final PoliceTickerRepository repository = mock(PoliceTickerRepository.class);

    private final LvzPoliceTickerCrawler crawler = mock(LvzPoliceTickerCrawler.class);

    private final LvzPoliceTickerDetailViewCrawler detailCrawler = mock(LvzPoliceTickerDetailViewCrawler.class);

    private final CrawlScheduler scheduler = new CrawlScheduler(repository, crawler, detailCrawler, null,
            mock(NominatimAsker.class));

    @Test
    @SuppressWarnings("unchecked")
    void crawlSkipsFailingArticleAndClosesBrowser() throws ExecutionException, InterruptedException {
        final var ticker = new PoliceTicker();
        ticker.setUrl(VALID_URL);
        when(crawler.execute()).thenReturn(new AsyncResult<>(Arrays.asList(BLOCKED_URL, VALID_URL)));
        when(detailCrawler.execute(BLOCKED_URL))
                .thenReturn(AsyncResult.forExecutionException(new IllegalStateException("article headline not found")));
        when(detailCrawler.execute(VALID_URL)).thenReturn(new AsyncResult<>(ticker));

        scheduler.crawl();

        final ArgumentCaptor<Iterable<PoliceTicker>> saved = ArgumentCaptor.forClass(Iterable.class);
        verify(repository).saveAll(saved.capture());
        assertThat(saved.getValue()).containsExactly(ticker);
        verify(detailCrawler).closeBrowser();
    }

    @Test
    void crawlClosesBrowserOnUnexpectedFailure() {
        when(crawler.execute()).thenReturn(new AsyncResult<>(List.of(VALID_URL)));
        doThrow(new RuntimeException("session lost")).when(detailCrawler).execute(anyString());

        assertThatThrownBy(scheduler::crawl).hasMessage("session lost");

        verify(detailCrawler).closeBrowser();
    }
}
