package de.codefor.le.crawler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.env.MockEnvironment;

class LvzPoliceTickerCrawlerTest {

    private final LvzPoliceTickerCrawler crawler = new LvzPoliceTickerCrawler(null, new CrawlerWebDriverFactory(new MockEnvironment()));

    @Test
    void testExecute() throws ExecutionException, InterruptedException {
        final var future = crawler.execute();
        assertThat(future).isNotNull().isNotCancelled();
        final var articleUrls = future.get();
        assertThat(articleUrls).isNotEmpty().allSatisfy(article -> {
            assertThat(article).startsWith(LvzPoliceTickerCrawler.LVZ_BASE_URL);
            assertThat(article).doesNotContain("Blitzer-in-Leipzig");
            assertThat(article).doesNotContain("polizeiticker-leipzig-aktuelle-polizeimeldungen");
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.example.com/lokales/leipzig/some-article.html",
            "https://www.lvz.de/Leipzig/Polizeiticker/Blitzer-in-Leipzig-Wo-wird-heute-geblitzt-1.-Maerz-2021",
            "https://www.lvz.de/lokales/leipzig/polizeiticker-leipzig-aktuelle-polizeimeldungen-in-und-um-leipzig-27-09-2026-BHAXNG753JAPLCPY3OIJJO6YRA.html"
    })
    void shouldSkipUrl(final String url) {
        assertThat(LvzPoliceTickerCrawler.shouldSkipUrl(url)).isTrue();
    }

    @Test
    void shouldNotSkipRegularArticle() {
        assertThat(LvzPoliceTickerCrawler.shouldSkipUrl(
                "https://www.lvz.de/lokales/leipzig/strassenbahn-kollidiert-im-leipziger-westen-mit-auto-IA455EPV6RFHBMD3756CUJJO7I.html"))
                .isFalse();
    }

}
