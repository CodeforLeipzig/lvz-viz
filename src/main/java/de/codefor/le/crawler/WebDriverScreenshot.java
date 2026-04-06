package de.codefor.le.crawler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WebDriverScreenshot {

    public static final String REASON_BLOCKED = "blocked";

    public static final String REASON_NO_SUCH_ELEMENT = "no_such_element";

    private static final String LOGS_DIR = "logs";

    private static final long MIN_INTERVAL_MILLIS = 60000;

    private static final int MAX_SCREENSHOTS = 50;

    private static long lastScreenshotTime = 0;

    private static final Logger logger = LoggerFactory.getLogger(WebDriverScreenshot.class);

    private WebDriverScreenshot() {
    }

    public static void take(final WebDriver driver, final String reason) {
        if (driver instanceof TakesScreenshot) {
            final long currentTime = System.currentTimeMillis();
            if (currentTime - lastScreenshotTime < MIN_INTERVAL_MILLIS) {
                logger.info("Skipping screenshot for '{}' (too frequent)", reason);
                return;
            }
            try {
                final var timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
                final var path = Paths.get(LOGS_DIR, reason + "_" + timestamp + ".png");
                Files.createDirectories(path.getParent());
                Files.write(path, ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
                logger.info("Screenshot saved to {}", path.toAbsolutePath());
                lastScreenshotTime = currentTime;
                cleanup();
            } catch (IOException ex) {
                logger.warn("Failed to save screenshot", ex);
            }
        }
    }

    private static void cleanup() {
        try (final var files = Files.list(Paths.get(LOGS_DIR))) {
            final var screenshotFiles = files.filter(path -> path.getFileName().toString().endsWith(".png"))
                    .sorted(Comparator.comparing(Path::toString)).collect(Collectors.toList());

            if (screenshotFiles.size() > MAX_SCREENSHOTS) {
                final int toDeleteCount = screenshotFiles.size() - MAX_SCREENSHOTS;
                for (int i = 0; i < toDeleteCount; i++) {
                    final var toDelete = screenshotFiles.get(i);
                    Files.deleteIfExists(toDelete);
                    logger.debug("Deleted old screenshot: {}", toDelete);
                }
            }
        } catch (IOException ex) {
            logger.warn("Failed to cleanup old screenshots", ex);
        }
    }

}
