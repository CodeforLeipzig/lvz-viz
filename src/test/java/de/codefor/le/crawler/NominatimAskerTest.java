package de.codefor.le.crawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import de.codefor.le.crawler.model.Nominatim;

class NominatimAskerTest {

    private final NominatimAsker asker = new NominatimAsker();

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " " })
    void executeWithEmptyAddress(final String input) {
        assertThat(asker.execute(input)).isNotNull();
    }

    @Test
    void executeWithValidDistrictInLeipzig() throws InterruptedException, ExecutionException {
        checkResults(asker.execute(NominatimAsker.NOMINATIM_SEARCH_CITY_PREFIX + "Grünau"));
    }

    @Test
    void executeWithValidAddressInLeipzig() throws InterruptedException, ExecutionException {
        checkResults(asker.execute(NominatimAsker.NOMINATIM_SEARCH_CITY_PREFIX + "Weißenfelser"));
    }

    @Test
    void executeSendsUserAgentHeader() throws InterruptedException, ExecutionException {
        final var restTemplate = mock(RestTemplate.class);
        final var entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        final var nominatim = new Nominatim();
        nominatim.setLat("51.34");
        nominatim.setLon("12.38");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), entityCaptor.capture(), eq(Nominatim[].class)))
                .thenReturn(ResponseEntity.ok(new Nominatim[]{ nominatim }));

        final var result = new NominatimAsker(restTemplate).execute(NominatimAsker.NOMINATIM_SEARCH_CITY_PREFIX + "Test").get();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).extracting(Nominatim::getLat, Nominatim::getLon).containsExactly("51.34", "12.38");
        assertThat(entityCaptor.getValue().getHeaders().getFirst(HttpHeaders.USER_AGENT))
                .isEqualTo(LvzPoliceTickerCrawler.USER_AGENT);
    }

    @Test
    void executeWithNullResponseBodyReturnsEmptyList() throws InterruptedException, ExecutionException {
        final var restTemplate = mock(RestTemplate.class);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Nominatim[].class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        final var result = new NominatimAsker(restTemplate).execute(NominatimAsker.NOMINATIM_SEARCH_CITY_PREFIX + "Test").get();

        assertThat(result).isEmpty();
    }

    private void checkResults(final Future<List<Nominatim>> future) throws InterruptedException, ExecutionException {
        assertThat(future).isNotNull();
        final var res = future.get();
        assertThat(res).isNotNull().hasSizeGreaterThanOrEqualTo(1);
        assertThat(res.get(0)).extracting(Nominatim::getLat, Nominatim::getLon).describedAs("lat/lon").isNotNull();
    }
}
