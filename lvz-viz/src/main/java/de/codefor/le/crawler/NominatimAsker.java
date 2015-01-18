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

    private final RestTemplate restTemplate;

    public NominatimAsker() {
        restTemplate = new RestTemplate();
    }

    @Async
    public Future<List<Nominatim>> execute(final String address) {
        List<Nominatim> result = null;
        try {
            result = getCoords(address);
            Thread.sleep(1);
        } catch (final InterruptedException e) {
            logger.error(e.toString(), e);
        }
        return new AsyncResult<List<Nominatim>>(result != null ? result : new ArrayList<Nominatim>());
    }

    private List<Nominatim> getCoords(final String address) {
        final String url = "http://nominatim.openstreetmap.org/search?q=" + address + "&format=json";
        logger.debug("url {}", url);

        final List<Nominatim> result = Arrays.asList(restTemplate.getForObject(url, Nominatim[].class));
        logger.debug("nominatim search result: {}", result);
        return result;
    }

}
