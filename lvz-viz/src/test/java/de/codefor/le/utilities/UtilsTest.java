package de.codefor.le.utilities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UtilsTest {

    private static final String EMPTY_STRING = "";

    private static final String EXAMPLE_ID = "123456";

    private static final String URL_70905 = "http://nachrichten.lvz-online.de/leipzig/polizeiticker/polizeiticker-leipzig/diebe-stehlen-sich-mit-eis-und-nuernberger-wuerstchen-davon/r-polizeiticker-leipzig-a-70905.html";

    private static final String URL_70889 = "http://nachrichten.lvz-online.de/leipzig/polizeiticker/polizeiticker-leipzig/starker-bartwuchs--mann-versucht-in-leipzig-gruenau-rucksack-voll-rasierer-zu-stehlen/r-polizeiticker-leipzig-a-70889.html";

    @Test
    public void extractArticleId() {
        assertEquals(EMPTY_STRING, Utils.extractArticleId(null));
        assertEquals(EMPTY_STRING, Utils.extractArticleId(EMPTY_STRING));
        assertEquals(EMPTY_STRING, Utils.extractArticleId(" "));
        assertEquals(EMPTY_STRING, Utils.extractArticleId("/12345"));
        assertEquals(EXAMPLE_ID, Utils.extractArticleId("/" + EXAMPLE_ID));
        assertEquals(EXAMPLE_ID, Utils.extractArticleId("/1234567"));
    }

    @Test
    public void generateHashForUrl() {
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", Utils.generateHashForUrl(null));
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", Utils.generateHashForUrl(EMPTY_STRING));
        assertEquals("195c390b14e62b9673ec093a6b44d8b7b93113a8", Utils.generateHashForUrl(URL_70905));
        assertEquals("724758f0e86e90ee131619d07505bc0b0704bc83", Utils.generateHashForUrl(URL_70889));
    }
}
