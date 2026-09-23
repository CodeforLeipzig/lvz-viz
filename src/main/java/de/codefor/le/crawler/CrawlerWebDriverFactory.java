package de.codefor.le.crawler;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import com.google.common.annotations.VisibleForTesting;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;

/**
 * Creates the Chrome instances used for crawling. lvz.de is behind a bot detection that blocks
 * plain HTTP clients and headless Chrome, so every request has to go through a real browser that
 * does not expose <code>navigator.webdriver</code>.
 */
@Component
@RequiredArgsConstructor
public class CrawlerWebDriverFactory {

    private final Environment environment;

    public WebDriver create() {
        final var options = new ChromeOptions()
                .addArguments("--disable-blink-features=AutomationControlled");
        final WebDriver driver = useRemoteDriver()
                ? WebDriverManager.chromedriver().remoteAddress("http://chrome:4444/wd/hub")
                        .capabilities(options).create()
                : new ChromeDriver(options);
        // implicitlyWait only covers looking up elements. Without a page load timeout the w3c
        // default of five minutes applies, and since the detail pages are crawled sequentially a
        // single stalling page would hold up the whole run.
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10)).pageLoadTimeout(Duration.ofSeconds(30));
        return driver;
    }

    /**
     * dev and prod run next to a selenium container, everything else starts a local Chrome. Checked
     * via {@link Environment#acceptsProfiles(Profiles)}, so additional active profiles do not matter.
     */
    @VisibleForTesting
    boolean useRemoteDriver() {
        return environment.acceptsProfiles(Profiles.of("dev | prod"));
    }
}
