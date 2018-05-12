package de.codefor.le.web;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
public class PoliceTickerController {

    private static final Logger logger = LoggerFactory.getLogger(PoliceTickerController.class);

    private final PoliceTickerRepository policeTickerRepository;

    private final ElasticsearchTemplate elasticsearchTemplate;

    private final Optional<NER> ner;

    @RequestMapping(value = "/getx", method = RequestMethod.GET)
    public Page<PoliceTicker> getx(@PageableDefault final Pageable pageable) {
        return policeTickerRepository.findAll(pageable);
    }

    @RequestMapping(value = "/extractlocations", method = RequestMethod.POST)
    public Iterable<String> getLocations(@RequestBody final String locations) {
        logger.debug("extractlocations: {}", locations);
        if (ner.isPresent()) {
            return ner.get().getLocations(locations, false);
        }
        logger.debug("return empty result b/c NER is not initialized!");
        return Collections.<String> emptyList();
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public Page<PoliceTicker> search(@RequestParam String query,
            @PageableDefault(direction = Direction.DESC, sort = "datePublished") final Pageable pageable) {
        logger.debug("search query: {}", query);
        Page<PoliceTicker> result;
        if (!query.isEmpty()) {
            query = query.toLowerCase();
            final List<String> splitToList = Splitter.on(CharMatcher.WHITESPACE).splitToList(query);

            final SearchQuery sq = new NativeSearchQueryBuilder().withPageable(pageable)
                    .withQuery(createFulltextSearchQueryBuilder(splitToList)).build();

            result = elasticsearchTemplate.queryForPage(sq, PoliceTicker.class);
        } else {
            result = getx(pageable);
        }
        return result;
    }

    @RequestMapping(value = "/searchbetween", method = RequestMethod.GET)
    public Page<PoliceTicker> searchBetween(@RequestParam(defaultValue = "") String query,
            @RequestParam @DateTimeFormat(iso = ISO.DATE_TIME) final LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE_TIME) final LocalDateTime to,
            @PageableDefault(direction = Direction.DESC, sort = "datePublished", size = Integer.MAX_VALUE) final Pageable pageable) {
        logger.debug("query: {} from: {}, to: {}", new Object[] { query, from, to });
        query = query.toLowerCase();
        final List<String> splitToList = Splitter.on(CharMatcher.WHITESPACE).splitToList(query);

        final SearchQuery sq = new NativeSearchQueryBuilder().withPageable(pageable)
                .withQuery(createFulltextSearchQueryBetween(splitToList, from, to)).build();
        final Page<PoliceTicker> results = elasticsearchTemplate.queryForPage(sq, PoliceTicker.class);
        logger.debug("results {}", results.getSize());
        return results;
    }

    @RequestMapping(value = "/between", method = RequestMethod.GET)
    public Page<PoliceTicker> between(@RequestParam @DateTimeFormat(iso = ISO.DATE_TIME) final LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE_TIME) final LocalDateTime to,
            @PageableDefault(direction = Direction.DESC, sort = "datePublished", page = 0, size = Integer.MAX_VALUE) final Pageable pageable) {
        logger.debug("from {}, to {}", from, to);
        final Page<PoliceTicker> between = policeTickerRepository.findByDatePublishedBetween(convertToDate(from), convertToDate(to),
                pageable);
        return between;
    }

    @RequestMapping(value = "/minmaxdate", method = RequestMethod.GET)
    public DateTime[] minMaxDate() {
        final Page<PoliceTicker> minDate = policeTickerRepository.findAll(new PageRequest(0, 1, Direction.ASC, "datePublished"));
        final Page<PoliceTicker> maxDate = policeTickerRepository.findAll(new PageRequest(0, 1, Direction.DESC, "datePublished"));
        final DateTime minDatePublished = new DateTime(minDate.getContent().get(0).getDatePublished());
        final DateTime maxDatePublished = new DateTime(maxDate.getContent().get(0).getDatePublished());
        logger.debug("min {}, max {}", minDatePublished, maxDatePublished);
        return new DateTime[] { minDatePublished, maxDatePublished };
    }

    @RequestMapping(value = "/last7days", method = RequestMethod.GET)
    public DateTime[] last7Days() {
        final DateTime now = DateTime.now();
        final DateTime minus7days = DateTime.now().minusDays(30);
        logger.debug("last7days: fromDate {}, toDate {}", minus7days, now);
        return new DateTime[] { minus7days, now };
    }

    private static BoolQueryBuilder createFulltextSearchQueryBetween(final List<String> splitToList, final LocalDateTime from,
            final LocalDateTime to) {
        BoolQueryBuilder searchQuery = null;
        if (splitToList.isEmpty()) {
            searchQuery = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery());
        } else {
            searchQuery = createFulltextSearchQueryBuilder(splitToList);
        }
        final RangeQueryBuilder rqb = QueryBuilders.rangeQuery("datePublished").from(convertToDate(from)).to(convertToDate(to));

        searchQuery.must(rqb);
        return searchQuery;
    }

    private static BoolQueryBuilder createFulltextSearchQueryBuilder(final List<String> splitToList) {
        final BoolQueryBuilder articleBool = QueryBuilders.boolQuery();
        final BoolQueryBuilder titleBool = QueryBuilders.boolQuery();
        for (final String s : splitToList) {
            articleBool.must(QueryBuilders.termQuery("article", s));
            titleBool.must(QueryBuilders.termQuery("title", s));

        }
        final BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.should(articleBool);
        boolQueryBuilder.should(titleBool);
        return boolQueryBuilder;
    }

    private static Date convertToDate(final LocalDateTime localeDateTime) {
        return Date.from(localeDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
