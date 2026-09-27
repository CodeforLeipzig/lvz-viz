package de.codefor.le.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import de.codefor.le.model.PoliceTicker;
import de.codefor.le.repositories.PoliceTickerRepository;

class PoliceTickerControllerResultWindowTest {

    private static final PageRequest BEYOND_RESULT_WINDOW = PageRequest.of(PoliceTickerController.MAX_RESULT_WINDOW / 5, 5);

    private final PoliceTickerRepository repository = mock(PoliceTickerRepository.class);

    private final ElasticsearchRestTemplate template = mock(ElasticsearchRestTemplate.class);

    private final PoliceTickerController controller = new PoliceTickerController(repository, template, Optional.empty());

    @Test
    void getxBeyondResultWindowReturnsTotal() {
        when(repository.count()).thenReturn(20_178L);

        final var result = controller.getx(BEYOND_RESULT_WINDOW);

        assertThat(result).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(20_178L);
        assertThat(result.getNumber()).isEqualTo(BEYOND_RESULT_WINDOW.getPageNumber());
        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    void searchBeyondResultWindowReturnsTotal() {
        when(template.count(any(SearchQuery.class), eq(PoliceTicker.class))).thenReturn(13_641L);

        final var result = controller.search("polizei", BEYOND_RESULT_WINDOW);

        assertThat(result).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(13_641L);
        verify(template, never()).queryForPage(any(SearchQuery.class), eq(PoliceTicker.class));
    }

    @Test
    void lastPageWithinResultWindowIsQueried() {
        final var lastPage = PageRequest.of(PoliceTickerController.MAX_RESULT_WINDOW / 5 - 1, 5);

        controller.getx(lastPage);

        verify(repository).findAll(lastPage);
        verify(repository, never()).count();
    }
}
