package de.codefor.le.crawler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

class SkipWhenBlockedTest {

    private final SkipWhenBlocked handler = new SkipWhenBlocked();

    @Test
    void abortsWhenBlocked() {
        assertThatThrownBy(() -> handler.handleTestExecutionException(null, new CrawlerBlockedException("blocked")))
                .isInstanceOf(TestAbortedException.class);
    }

    @Test
    void abortsWhenBlockedInsideFuture() {
        final var wrapped = new ExecutionException(new CrawlerBlockedException("blocked"));
        assertThatThrownBy(() -> handler.handleTestExecutionException(null, wrapped))
                .isInstanceOf(TestAbortedException.class);
    }

    @Test
    void rethrowsAnyOtherFailure() {
        final var failure = new IllegalStateException("article headline not found");
        assertThatThrownBy(() -> handler.handleTestExecutionException(null, failure)).isSameAs(failure);
    }
}
