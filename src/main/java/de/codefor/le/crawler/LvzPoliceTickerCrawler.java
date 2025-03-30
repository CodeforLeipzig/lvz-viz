package de.codefor.le.crawler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import org.htmlunit.BrowserVersion;
import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.WebClient;
import org.htmlunit.WebClientOptions;
import org.htmlunit.WebRequest;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlPage;
import com.google.common.base.Stopwatch;

import de.codefor.le.repositories.PoliceTickerRepository;
import de.codefor.le.utilities.Utils;
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

    private WebClient webClient;
    private HtmlPage htmlPage;

    @Async
    public Future<Iterable<String>> execute() {
        final var watch = Stopwatch.createStarted();
        final var url = LVZ_POLICE_TICKER_BASE_URL;
        logger.debug("Start crawling {}.", url);
        try {
            return new AsyncResult<>(crawlNewsFromPage(url));
        } finally {
            watch.stop();
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
        initWebClient();
        WebRequest request;
        try {
            request = new WebRequest(new URI(url).toURL());
            request.setCharset(StandardCharsets.UTF_8);
            htmlPage = webClient.getPage(request);
        } catch (IOException | URISyntaxException | FailingHttpStatusCodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // javascript is disabled so the frame doesn't appear
        // logger.debug("accept cookies first, it's an iframe");

        // workaround: click only ten times and avoid "endless" loading
        for (int i = 0; i < 10; i++) {
            if (!loadMoreArticles()) {
                logger.debug("Stop loading more articles.");
                break;
            }
        }

        return htmlPage.querySelector("#fusion-app").asXml();
    }

    /**
     * Load more articles via specific button.
     *
     * @return true, if exactly one button was found
     */
    private boolean loadMoreArticles() {
        final var domNodes = htmlPage.querySelectorAll("div[class*=LoadMorestyled__Button] button");
        final Collection<DomElement> domElements = domNodes.stream()
                .filter(DomElement.class::isInstance)
                .map(DomElement.class::cast).collect(Collectors.toList());
        final var size = domElements.size();
        if (size != 1) {
            if (size != 0 && logger.isDebugEnabled()) {
                logger.debug("available buttons: {}",
                        domElements.stream().map(e -> e.getAttribute("class"))
                                .collect(Collectors.joining(", ")));
            }
            logger.warn("unexpected number of buttons: {}", size);
            return false;
        }
        final DomElement element = domElements.stream().findFirst().get();
        if ("Mehr anzeigen".equals(element.getTextContent())) {
            if (logger.isDebugEnabled()) {
                logger.debug("load more articles via button {}", element.getAttribute("class"));
            }
            try {
                element.click();
            } catch (IOException e) {
                logger.warn("Unable to click element {}", element.getAttribute("class"));
                logger.debug("Cause", e);
                return false;
            }
        }
        return true;
    }

    /**
     * Init webclient with chrome browser and some options.
     */
    private void initWebClient() {
        webClient = new WebClient(BrowserVersion.CHROME);
        final WebClientOptions options = webClient.getOptions();
        options.setJavaScriptEnabled(false);
        options.setUseInsecureSSL(true);
        options.setThrowExceptionOnScriptError(false);
        options.setThrowExceptionOnFailingStatusCode(false);
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
