package de.codefor.le.crawler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.env.MockEnvironment;

class CrawlerWebDriverFactoryTest {

    @ParameterizedTest
    @CsvSource({
            "'', false",
            "test, false",
            "dev, true",
            "prod, true",
            "'prod,debug', true",
            "'test,debug', false"
    })
    void useRemoteDriver(final String profiles, final boolean expected) {
        final var environment = new MockEnvironment();
        environment.setActiveProfiles(profiles.isEmpty() ? new String[0] : profiles.split(","));
        assertThat(new CrawlerWebDriverFactory(environment).useRemoteDriver()).isEqualTo(expected);
    }
}
