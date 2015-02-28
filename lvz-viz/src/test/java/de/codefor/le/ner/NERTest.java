package de.codefor.le.ner;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Test;

public class NERTest {

    @Test
    public void initBlackListedLocations() {
        final Collection<String> blackListedLocations = NER.initBlackListedLocations();
        assertNotNull(blackListedLocations);
        assertThat(blackListedLocations, hasItem("Leipzig"));
        assertThat(blackListedLocations, hasItem("Dresdens"));
        assertThat(blackListedLocations, not(hasItem("# federal states")));
        assertThat(blackListedLocations, not(hasItem("")));
    }

    @Test
    public void getLocations() {
        final NER ner = new NER();
        assertNotNull(ner.getLocations("foo", false));
        assertNotNull(ner.getLocations("foo", true));
    }

}
