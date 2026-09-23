package de.codefor.le.crawler;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.opentest4j.TestAbortedException;

/**
 * Reports a live test as skipped instead of failed when lvz.de blocks the crawler. The block
 * depends on the ip of the machine running the test (shared CI runners get flagged regularly), so
 * it says nothing about the code under test. Any other failure is rethrown unchanged.
 */
class SkipWhenBlocked implements TestExecutionExceptionHandler {

    @Override
    public void handleTestExecutionException(final ExtensionContext context, final Throwable throwable)
            throws Throwable {
        for (var cause = throwable; cause != null; cause = cause.getCause()) {
            if (cause instanceof CrawlerBlockedException) {
                throw new TestAbortedException("lvz.de blocked the crawler: " + cause.getMessage(), throwable);
            }
        }
        throw throwable;
    }
}
