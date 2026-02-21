package de.codefor.le.crawler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public Future<List<Nominatim>> execute(final String address) {
        List<Nominatim> result;
        if (address != null && !address.isBlank()) {
            final var url = String.format(NOMINATIM_SEARCH_URL, address);
            logger.debug("url {}", url);
            final var headers = new HttpHeaders();
            headers.set(HttpHeaders.USER_AGENT, LvzPoliceTickerCrawler.USER_AGENT);
            final var entity = new HttpEntity<>(headers);
            result = Arrays.asList(Optional.ofNullable(restTemplate.exchange(url, HttpMethod.GET, entity, Nominatim[].class).getBody()).orElse(new Nominatim[0]));
        } else {
            result = Collections.emptyList();
        }

        logger.debug("nominatim search result: {}", result);
        return new AsyncResult<>(result);
    }
}
