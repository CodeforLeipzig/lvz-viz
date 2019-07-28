package de.codefor.le.ner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("crawl")
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = { "app.initialDelay=300000" })
public class NERTest {

    @Autowired
    private NER ner;

    @Test
    public void initBlackListedLocations() {
        assertThat(ner.getBlackListedLocations()).isNotNull().contains("Leipzig", "Dresdens")
                .doesNotContain("# federal states", "");
    }

    @Test
    public void getLocationsForSimpleStringShouldReturnNull() {
        assertThat(ner.getLocations("foo", false)).isNotNull();
        assertThat(ner.getLocations("foo", true)).isNotNull();
    }

    private static final String ARTICLE = "Leipzig. Der Autoanhänger [...] ist am Montagabend in Leipzig-Plagwitz von [...]"
            + "Der Vorfall ereignete sich auf der Karl-Heine-Straße [...]"
            + "Eine der Flaschen zerschellte auf der Karl-Heine-Straße in Höhe König-Albert Brücke. [...] Danach fuhr das Auto weiter."
            + "[...] Von LVZ";

    @Test
    public void getLocationsForArticleShouldReturnCollection() {
        assertThat(ner.getLocations(ARTICLE, true)).isNotNull().containsExactlyInAnyOrder("Leipzig-Plagwitz",
                "Karl-Heine-Straße", "Brücke");
    }
}
