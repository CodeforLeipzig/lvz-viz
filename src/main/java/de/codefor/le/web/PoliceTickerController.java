package de.codefor.le.web;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

import de.codefor.le.model.PoliceTicker;
import de.codefor.le.ner.NER;
import de.codefor.le.repositories.PoliceTickerRepository;
import lombok.RequiredArgsConstructor;

@RequestMapping(value = "/api")
@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "http://lvzviz.inpercima.net", maxAge = 3600)
public class PoliceTickerController {

    private static final Logger logger = LoggerFactory.getLogger(PoliceTickerController.class);

    private static final String DATE_PUBLISHED = "datePublished";

    private final PoliceTickerRepository policeTickerRepository;

    private final ElasticsearchRestTemplate elasticsearchTemplate;

    private final Optional<NER> ner;

    @GetMapping(value = "/getx")
    public Page<PoliceTicker> getx(@PageableDefault final Pageable pageable) {
        return policeTickerRepository.findAll(pageable);
    }

    @PostMapping(value = "/extractlocations")
    public Iterable<String> getLocations(@RequestBody final String locations) {
        logger.debug("extractlocations: {}", locations);
        return ner.map(n -> n.getLocations(locations, false)).orElseGet(() -> {
            logger.debug("return empty result b/c NER is not initialized!");
            return Collections.emptyList();
        });
    }

    @GetMapping(value = "/search")
    public Page<PoliceTicker> search(@RequestParam final String query,
            @PageableDefault(direction = Direction.DESC, sort = DATE_PUBLISHED) final Pageable pageable) {
        logger.debug("search query: {}", query);
        return query.isEmpty() ? getx(pageable)
                : elasticsearchTemplate.queryForPage(
                        new NativeSearchQueryBuilder().withPageable(pageable)
                                .withQuery(createFulltextSearchQueryBuilder(splitIntoTerms(query))).build(),
                        PoliceTicker.class);
    }

    @GetMapping(value = "/searchbetween")
    public Page<PoliceTicker> searchBetween(@RequestParam(defaultValue = "") final String query,
            @RequestParam @DateTimeFormat(iso = ISO.DATE_TIME) final LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE_TIME) final LocalDateTime to,
            @PageableDefault(direction = Direction.DESC, sort = DATE_PUBLISHED, size = Integer.MAX_VALUE) final Pageable pageable) {
        logger.debug("query: {} from: {}, to: {}", query, from, to);
        final var results = elasticsearchTemplate
                .queryForPage(
                        new NativeSearchQueryBuilder().withPageable(pageable)
                                .withQuery(createFulltextSearchQueryBetween(query, from, to)).build(),
                        PoliceTicker.class);
        logger.debug("results {}", results.getSize());
        return results;
    }

    @GetMapping(value = "/minmaxdate")
    public DateTime[] minMaxDate() {
        final var minDate = policeTickerRepository.findAll(PageRequest.of(0, 1, Direction.ASC, DATE_PUBLISHED));
        final var maxDate = policeTickerRepository.findAll(PageRequest.of(0, 1, Direction.DESC, DATE_PUBLISHED));
        final var minDatePublished = minDate.getContent().size() > 0
                ? new DateTime(minDate.getContent().get(0).getDatePublished())
                : DateTime.now();
        final var maxDatePublished = maxDate.getContent().size() > 0
                ? new DateTime(maxDate.getContent().get(0).getDatePublished())
                : DateTime.now();
        logger.debug("min {}, max {}", minDatePublished, maxDatePublished);
        return new DateTime[] { minDatePublished, maxDatePublished };
    }

    @GetMapping(value = "/last7days")
    public DateTime[] last7Days() {
        final var now = DateTime.now();
        final var minus7days = DateTime.now().minusDays(7);
        logger.debug("last7days: fromDate {}, toDate {}", minus7days, now);
        return new DateTime[] { minus7days, now };
    }

    private static List<String> splitIntoTerms(final String query) {
        return Splitter.on(CharMatcher.whitespace()).splitToList(query.toLowerCase());
    }

    private static BoolQueryBuilder createFulltextSearchQueryBetween(final String query, final LocalDateTime from,
            final LocalDateTime to) {
        final var terms = splitIntoTerms(query);
        return (terms.isEmpty() ? QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery())
                : createFulltextSearchQueryBuilder(terms))
                        .must(QueryBuilders.rangeQuery(DATE_PUBLISHED).from(convertToDate(from)).to(convertToDate(to)));
    }

    private static BoolQueryBuilder createFulltextSearchQueryBuilder(final List<String> terms) {
        final var articleBool = QueryBuilders.boolQuery();
        final var titleBool = QueryBuilders.boolQuery();
        for (final String term : terms) {
            articleBool.must(QueryBuilders.termQuery("article", term));
            titleBool.must(QueryBuilders.termQuery("title", term));
        }
        return QueryBuilders.boolQuery().should(articleBool).should(titleBool);
    }

    private static DateTime convertToDate(final LocalDateTime localeDateTime) {
        return new DateTime(Date.from(localeDateTime.atZone(ZoneId.systemDefault()).toInstant()));
    }
}
