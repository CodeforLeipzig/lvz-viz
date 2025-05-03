package de.codefor.le.ner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class NERTest {

    @Autowired
    private NER ner;

    @Test
    void initUnspecificLocations() {
        assertThat(ner.getUnspecificLocations())
            .contains("Leipzig", "Dresdens")
            .doesNotContain("# federal states", "");
    }

    @Test
    void getLocationsForSimpleStringShouldReturnEmptyCollection() {
        assertThat(ner.getLocations(null, false)).isEmpty();
        assertThat(ner.getLocations("", false)).isEmpty();
        assertThat(ner.getLocations("foo", false)).isEmpty();
        assertThat(ner.getLocations("foo", true)).isEmpty();
    }

    private static final String ARTICLE = "Leipzig. Der Autoanhänger [...] ist am Montagabend in Leipzig-Plagwitz von [...]"
            + "Der Vorfall ereignete sich auf der Karl-Heine-Straße [...]"
            + "Eine der Flaschen zerschellte auf der Karl-Heine-Straße in Höhe König-Albert Brücke. [...] Danach fuhr das Auto weiter."
            + "[...] Von LVZ";

    @Test
    void getLocationsForArticleShouldReturnCollection() {
        assertThat(ner.getLocations(ARTICLE, true))
            .containsExactly("Leipzig-Plagwitz", "Karl-Heine-Straße", "König-Albert-Brücke");
    }
}
