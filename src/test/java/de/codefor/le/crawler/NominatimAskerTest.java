package de.codefor.le.crawler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import de.codefor.le.crawler.model.Nominatim;

public class NominatimAskerTest {

    private final NominatimAsker asker = new NominatimAsker();

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " " })
    public void executeWithEmptyAddress(final String input) {
        assertThat(asker.execute(input)).isNotNull();
    }

    @Test
    public void executeWithValidDistrictInLeipzig() throws InterruptedException, ExecutionException {
        checkResults(asker.execute(NominatimAsker.NOMINATIM_SEARCH_CITY_PREFIX + "Grünau"));
    }

    @Test
    public void executeWithValidAddressInLeipzig() throws InterruptedException, ExecutionException {
        checkResults(asker.execute(NominatimAsker.NOMINATIM_SEARCH_CITY_PREFIX + "Weißenfelser"));
    }

    private void checkResults(final Future<List<Nominatim>> future) throws InterruptedException, ExecutionException {
        assertThat(future).isNotNull();
        final var res = future.get();
        assertThat(res).isNotNull().hasSizeGreaterThanOrEqualTo(1);
        assertThat(res.get(0)).extracting(Nominatim::getLat, Nominatim::getLon).describedAs("lat/lon").isNotNull();
    }
}
