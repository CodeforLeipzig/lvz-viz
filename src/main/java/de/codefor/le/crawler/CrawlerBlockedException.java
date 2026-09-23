package de.codefor.le.crawler;

/**
 * Thrown when lvz.de answers with its bot detection block page instead of the requested content.
 * The block holds for the ip for a while, so retrying right away does not help.
 */
public class CrawlerBlockedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CrawlerBlockedException(final String message) {
        super(message);
    }
}
