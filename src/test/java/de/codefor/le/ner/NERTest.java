package de.codefor.le.ner;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NERTest {

    @Autowired
    private NER ner;

    @Test
    public void initBlackListedLocations() {
        final Collection<String> blackListedLocations = ner.initBlackListedLocations();
        assertNotNull(blackListedLocations);
        assertThat(blackListedLocations, hasItem("Leipzig"));
        assertThat(blackListedLocations, hasItem("Dresdens"));
        assertThat(blackListedLocations, not(hasItem("# federal states")));
        assertThat(blackListedLocations, not(hasItem("")));
    }

    @Test
    public void getLocationsForSimpleStringShouldReturnNull() {
        assertNotNull(ner.getLocations("foo", false));
        assertNotNull(ner.getLocations("foo", true));
    }

    private static final String ARTICLE = "Leipzig. Der Autoanhänger [...] ist am Montagabend in Leipzig-Plagwitz von [...]"
            + "Der Vorfall ereignete sich auf der Karl-Heine-Straße [...]"
            + "Eine der Flaschen zerschellte auf der Karl-Heine-Straße in Höhe König-Albert Brücke. [...] Danach fuhr das Auto weiter."
            + "[...] Von LVZ";

    @Test
    public void getLocationsForArticleShouldReturnCollection() {
        final Collection<String> locations = ner.getLocations(ARTICLE, true);
        assertNotNull(locations);
        assertThat(locations, Matchers.containsInAnyOrder("Leipzig-Plagwitz", "Karl-Heine-Straße", "Brücke"));
    }
}
