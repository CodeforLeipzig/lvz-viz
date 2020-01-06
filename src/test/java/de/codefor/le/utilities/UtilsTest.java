package de.codefor.le.utilities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

public class UtilsTest {

    private static final String URL_70905 = "http://nachrichten.lvz-online.de/leipzig/polizeiticker/polizeiticker-leipzig/diebe-stehlen-sich-mit-eis-und-nuernberger-wuerstchen-davon/r-polizeiticker-leipzig-a-70905.html";

    private static final String URL_70889 = "http://nachrichten.lvz-online.de/leipzig/polizeiticker/polizeiticker-leipzig/starker-bartwuchs--mann-versucht-in-leipzig-gruenau-rucksack-voll-rasierer-zu-stehlen/r-polizeiticker-leipzig-a-70889.html";

    @Test
    void generateHashForUrl() {
        assertThat(Utils.generateHashForUrl(URL_70905)).isEqualTo("195c390b14e62b9673ec093a6b44d8b7b93113a8");
        assertThat(Utils.generateHashForUrl(URL_70889)).isEqualTo("724758f0e86e90ee131619d07505bc0b0704bc83");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void generateHashForUrlWithEmptyValues(final String input) {
        assertThat(Utils.generateHashForUrl(input)).isEqualTo("da39a3ee5e6b4b0d3255bfef95601890afd80709");
    }
}
