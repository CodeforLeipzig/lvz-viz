package de.codefor.le.crawler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import de.codefor.le.crawler.model.Nominatim;

@Component
public class NominatimAsker {

    private static final Logger logger = LoggerFactory.getLogger(NominatimAsker.class);

    public static final String NOMINATIM_SEARCH_CITY_PREFIX = "Leipzig, ";

    private static final String NOMINATIM_SEARCH_URL = "https://nominatim.openstreetmap.org/search?q=%s&format=json";

    private static final int WAIT_BEFORE_EACH_ACCESS_TO_PREVENT_BANNING = 50;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public Future<List<Nominatim>> execute(final String address) {
        List<Nominatim> result = null;
        try {
            result = getCoords(address);
            Thread.sleep(WAIT_BEFORE_EACH_ACCESS_TO_PREVENT_BANNING);
        } catch (final InterruptedException e) {
            logger.warn(e.toString(), e);
            Thread.currentThread().interrupt();
        }
        return new AsyncResult<>(result != null ? result : new ArrayList<Nominatim>());
    }

    private List<Nominatim> getCoords(final String address) {
        final String url = String.format(NOMINATIM_SEARCH_URL, address);
        logger.debug("url {}", url);

        final List<Nominatim> result = Arrays.asList(restTemplate.getForObject(url, Nominatim[].class));
        logger.debug("nominatim search result: {}", result);
        return result;
    }
}
