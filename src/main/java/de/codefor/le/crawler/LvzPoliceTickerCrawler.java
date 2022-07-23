package de.codefor.le.crawler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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

    protected static final String LVZ_BASE_URL = "http://www.lvz.de";

    protected static final String LVZ_POLICE_TICKER_BASE_URL = LVZ_BASE_URL + "/themen/leipzig-polizei";

    private final Optional<PoliceTickerRepository> policeTickerRepository;

    @Value("${app.crawlAllMainPages}")
    private boolean crawlAllMainPages;

    /** hint for crawling the next site */
    private boolean crawlMore = true;

    private WebDriver driver;

    @Async
    public Future<Iterable<String>> execute(final int page) {
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
            logger.debug("Finished crawling page {} in {} ms.", page, watch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    /**
     * @param url the url to crawl
     * @return links of new articles
     */
    private Collection<String> crawlNewsFromPage(final String url) {
        final var doc = Jsoup.parse(initLoad(url));
        final var links = doc.select("a[class*=ContentTeaserstyled__Link]");
        final var result = extractNewArticleLinks(links);
        if (links.isEmpty()) {
            logger.debug("No links found on current page. This should be the last available page.");
            this.crawlMore = false;
        } else if (result.isEmpty()) {
            logger.debug("No new articles found on current page. {}",
                    crawlAllMainPages ? "Nevertheless, continue crawling on next page." : "Stop crawling for now.");
            this.crawlMore = crawlAllMainPages;
        } else {
            logger.info("{} new articles found on current page.", result.size());
        }
        return result;
    }

    private String initLoad(final String url) {
        initWebDriver();
        driver.get(url);

        // accept cookies first, it's an iframe
        driver.switchTo().frame(driver.findElement(By.cssSelector("iframe[id*=sp_message_iframe]")));
        driver.findElement(By.cssSelector("button[title=\"Alle akzeptieren\"]")).click();

        // switch back to main page after accept cookies and load more articles
        driver.switchTo().parentFrame();
        loadMoreArticles();

        return driver.findElement(By.id("fusion-app")).getAttribute("innerHTML");
    }

    private void loadMoreArticles() {
        final WebElement element = driver.findElement(By.cssSelector("div[class*=LoadMorestyled__Button] button"));
        if ("Mehr anzeigen".equals(element.getText())) {
            logger.debug("load 10 more articles");
            element.click();
            loadMoreArticles();
        }
    }

    private void initWebDriver() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(new ChromeOptions().setHeadless(true));
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    private Collection<String> extractNewArticleLinks(final Elements links) {
        final Collection<String> result = new ArrayList<>(links.size());
        for (final var link : links) {
            final String detailLink = LVZ_BASE_URL + link.attr("href");
            logger.debug("article url: {}", detailLink);
            if (!detailLink.startsWith(LVZ_BASE_URL)) {
                logger.debug("article not from policeticker - skip it");
                continue;
            } else if (detailLink.matches("(.*)Blitzer(.*)-in-Leipzig(.*)")) {
                logger.debug("recurring speed control article - skip it");
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

    public void resetCrawler() {
        this.crawlMore = true;
    }

    public boolean isMoreToCrawl() {
        return crawlMore;
    }

}
