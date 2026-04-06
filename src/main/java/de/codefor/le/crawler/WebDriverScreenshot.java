package de.codefor.le.crawler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WebDriverScreenshot {

    private static final Logger logger = LoggerFactory.getLogger(WebDriverScreenshot.class);

    private WebDriverScreenshot() {
    }

    public static void take(final WebDriver driver, final String reason) {
        if (driver instanceof TakesScreenshot) {
            try {
                final var timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
                final var path = Paths.get("logs", reason + "_" + timestamp + ".png");
                Files.createDirectories(path.getParent());
                Files.write(path, ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
                logger.info("Screenshot saved to {}", path.toAbsolutePath());
            } catch (IOException ex) {
                logger.warn("Failed to save screenshot", ex);
            }
        }
    }

}
