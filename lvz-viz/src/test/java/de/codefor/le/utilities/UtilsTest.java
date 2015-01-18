package de.codefor.le.utilities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UtilsTest {

    private static final String EMPTY_STRING = "";
    private static final String EXAMPLE_ID = "123456";

    @Test
    public void testGetArticleId() {
        assertEquals(EMPTY_STRING, Utils.getArticleId(null));
        assertEquals(EMPTY_STRING, Utils.getArticleId(EMPTY_STRING));
        assertEquals(EMPTY_STRING, Utils.getArticleId(" "));
        assertEquals(EMPTY_STRING, Utils.getArticleId("/12345"));
        assertEquals(EXAMPLE_ID, Utils.getArticleId("/" + EXAMPLE_ID));
        assertEquals(EXAMPLE_ID, Utils.getArticleId("/1234567"));
    }
}
