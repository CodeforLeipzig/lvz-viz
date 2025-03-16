package de.codefor.le.crawler;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import com.google.common.base.Stopwatch;

import de.codefor.le.repositories.PoliceTickerRepository;
import de.codefor.le.utilities.Utils;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LvzPoliceTickerCrawler {

    private static final Logger logger = LoggerFactory.getLogger(LvzPoliceTickerCrawler.class);

    protected static final String USER_AGENT = "leipzig crawler";

    protected static final int REQUEST_TIMEOUT = 30000;

    protected static final String LVZ_BASE_URL = "https://www.lvz.de";

    protected static final String LVZ_POLICE_TICKER_BASE_URL = LVZ_BASE_URL + "/themen/leipzig-polizei";

    private final Optional<PoliceTickerRepository> policeTickerRepository;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    private WebDriver driver;

    @Async
    public Future<Iterable<String>> execute() {
        final var watch = Stopwatch.createStarted();
        final var url = LVZ_POLICE_TICKER_BASE_URL;
        logger.debug("Start crawling {}.", url);
        try {
            return new AsyncResult<>(crawlNewsFromPage(url));
        } finally {
            watch.stop();
            if (driver != null) {
                driver.quit();
            }
            logger.debug("Finished crawling in {} ms.", watch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    /**
     * @param url the url to crawl
     * @return links of new articles
     */
    private Collection<String> crawlNewsFromPage(final String url) {
        final var doc = Jsoup.parse(initLoad(url));
        final var links = doc.select("a[class*=TeaserLinkstyled__Link]");
        final var result = extractNewArticleLinks(links);
        if (links.isEmpty()) {
            logger.warn("No links found.");
        } else if (result.isEmpty()) {
            logger.info("No new articles found.");
        } else {
            logger.info("{} new articles found.", result.size());
        }
        return result;
    }

    private String initLoad(final String url) {
        initWebDriver();
        driver.get(url);

        logger.debug("accept cookies first, it's an iframe");
        driver.switchTo().frame(driver.findElement(By.cssSelector("iframe[id*=sp_message_iframe]")));
        final var button = driver.findElement(By.cssSelector("button[title=\"Einwilligen und weiter\"]"));
        new Actions(driver).moveToElement(button).click().build().perform();

        // switch back to main page after accept cookies and load more articles
        driver.switchTo().parentFrame();

        // workaround: click only ten times and avoid "endless" loading
        for (int i = 0; i < 10; i++) {
            if (!loadMoreArticles()) {
                logger.info("stop loading");
                break;
            }
        }

        return driver.findElement(By.id("fusion-app")).getDomProperty("innerHTML");
    }

    /**
     * Load more articles via specific button.
     *
     * @return true, if exactly one button was found
     */
    private boolean loadMoreArticles() {
        final var elements = driver.findElements(By.cssSelector("div[class*=LoadMorestyled__Button] button"));
        final var size = elements.size();
        if (size != 1) {
            if (size != 0 && logger.isDebugEnabled()) {
                logger.debug("available buttons: {}",
                        elements.stream().map(e -> e.getDomAttribute("class")).collect(Collectors.joining(", ")));
            }
            logger.warn("unexpected number of buttons: {}", size);
            return false;
        }
        final WebElement element = elements.get(0);
        if ("Mehr anzeigen".equals(element.getText())) {
            if (logger.isDebugEnabled()) {
                logger.debug("load more articles via button {}", element.getDomAttribute("class"));
            }
            try {
                element.click();
            } catch (ElementNotInteractableException e) {
                logger.warn("Unable to click element {}", element.getDomAttribute("class"));
                logger.debug("Cause", e);
                return false;
            }
        }
        return true;
    }

    private void initWebDriver() {
        WebDriverManager.chromedriver().setup();
        driver = "dev".equals(activeProfile) || "prod".equals(activeProfile) ?
                WebDriverManager.chromedriver().remoteAddress("http://chrome:4444/wd/hub").create() :
                new ChromeDriver(new ChromeOptions().addArguments("--headless"));
        if (driver == null) {
            throw new IllegalStateException("initWebDriver for crawling failed");
        }
        logger.debug("initWebDriver for crawling succeeded");
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    private Collection<String> extractNewArticleLinks(final Elements links) {
        final Collection<String> result = new ArrayList<>(links.size());
        for (final var link : links) {
            final var href = link.attr("href");
            final var detailLink = !href.startsWith("http") ? LVZ_BASE_URL + href : href;
            logger.debug("article url: {}", detailLink);
            if (shouldSkipUrl(detailLink)) {
                continue;
            }
            policeTickerRepository.ifPresentOrElse(repo -> {
                if (!repo.existsById(Utils.generateHashForUrl(detailLink))) {
                    logger.debug("article not stored yet - save it");
                    result.add(detailLink);
                } else {
                    logger.debug("article already stored - skip it");
                }
            }, () -> result.add(detailLink));
        }
        return result;
    }

    private static boolean shouldSkipUrl(final String detailLink) {
        if (!detailLink.startsWith(LVZ_BASE_URL)) {
            logger.debug("article not from policeticker - skip it");
            return true;
        } else if (detailLink.matches("(.*)Blitzer(.*)-in-Leipzig(.*)")) {
            logger.debug("recurring speed control article - skip it");
            return true;
        }
        return false;
    }

}
