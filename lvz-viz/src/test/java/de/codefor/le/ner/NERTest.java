package de.codefor.le.ner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class NERTest {

    @Test
    public void initBlackListedLocations() {
        final List<String> blackListedLocations = NER.initBlackListedLocations();
        assertNotNull(blackListedLocations);
        final String firstElement = blackListedLocations.get(0);
        assertNotNull(firstElement);
        assertEquals("Leipzig", firstElement);
    }

    @Test
    @Ignore
    public void getLocations() {
        final NER ner = new NER();
        ner.getLocations("foo", false);
        ner.getLocations("foo", true);
    }

}
