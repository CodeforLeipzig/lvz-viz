package de.codefor.le.crawler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Strings;

import de.codefor.le.crawler.model.Nominatim;

@Component
public class NominatimAsker {

    private static final Logger logger = LoggerFactory.getLogger(NominatimAsker.class);

    public static final String NOMINATIM_SEARCH_CITY_PREFIX = "Leipzig, ";

    private static final String NOMINATIM_SEARCH_URL = "https://nominatim.openstreetmap.org/search?q=%s&format=json";

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public Future<List<Nominatim>> execute(final String address) {
        List<Nominatim> result;
        if (!Strings.isNullOrEmpty(address)) {
            final var url = String.format(NOMINATIM_SEARCH_URL, address);
            logger.debug("url {}", url);
            result = Arrays.asList(restTemplate.getForObject(url, Nominatim[].class));
        } else {
            result = Collections.emptyList();
        }

        logger.debug("nominatim search result: {}", result);
        return new AsyncResult<>(result);
    }
}
