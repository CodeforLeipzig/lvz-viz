package de.codefor.le.web;

import java.util.Collections;
import java.util.List;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

import de.codefor.le.model.PoliceTicker;
import de.codefor.le.ner.NER;
import de.codefor.le.repositories.PoliceTickerRepository;

@Controller
public class PoliceTickerController {

    private static final Logger logger = LoggerFactory.getLogger(PoliceTickerController.class);
    @Autowired
    private PoliceTickerRepository policeTickerRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired(required = false)
    private NER ner;

    @RequestMapping(value = "/getx", method = RequestMethod.GET)
    @ResponseBody
    public Page<PoliceTicker> getx(@PageableDefault Pageable pageable) {
        return policeTickerRepository.findAll(pageable);
    }

    @RequestMapping(value = "/extractlocations", method = RequestMethod.POST)
    @ResponseBody
    public Iterable<String> getLocations(@RequestBody String locations) {
        logger.debug("extractlocations: {}", locations);
        if (ner == null) {
            logger.debug("return empty result b/c NER is not initialized!");
        }
        return ner != null ? ner.getLocations(locations, false) : Collections.<String> emptyList();
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ResponseBody
    public Page<PoliceTicker> search(@RequestParam String query,
            @PageableDefault(direction = Direction.DESC, sort = "datePublished") Pageable pageable) {
        logger.info("query: {}", query);
        Page<PoliceTicker> result;
        if (!query.isEmpty()) {
            query = query.toLowerCase();
            final List<String> splitToList = Splitter.on(CharMatcher.WHITESPACE).splitToList(query);

            final SearchQuery sq = new NativeSearchQueryBuilder().withPageable(pageable)
                    .withQuery(createFulltextSearchQueryBuilder(pageable, splitToList)).build();

            elasticsearchTemplate.queryForPage(sq, PoliceTicker.class);
            result = elasticsearchTemplate.queryForPage(sq, PoliceTicker.class);
        } else {
            result = getx(pageable);
        }
        return result;
    }

    @RequestMapping(value = "/searchbetween", method = RequestMethod.GET)
    @ResponseBody
    public Page<PoliceTicker> searchBetween(
            @RequestParam(defaultValue = "") String query,
            @RequestParam String from,
            @RequestParam String to,
            @PageableDefault(direction = Direction.DESC, sort = "datePublished", size = Integer.MAX_VALUE) Pageable pageable) {
        logger.debug("query: {} from: {}, to: {}", new Object[] { query, from, to });
        query = query.toLowerCase();
        final List<String> splitToList = Splitter.on(CharMatcher.WHITESPACE).splitToList(query);

        final SearchQuery sq = new NativeSearchQueryBuilder().withPageable(pageable)
                .withQuery(createFulltextSearchQueryBetween(pageable, splitToList, from, to)).build();
        final Page<PoliceTicker> results = elasticsearchTemplate.queryForPage(sq, PoliceTicker.class);
        logger.debug("results {}", results.getSize());
        return results;
    }

    @RequestMapping(value = "/between", method = RequestMethod.GET)
    @ResponseBody
    public Page<PoliceTicker> between(
            @RequestParam String from,
            @RequestParam String to,
            @PageableDefault(direction = Direction.DESC, sort = "datePublished", page = 0, size = Integer.MAX_VALUE) Pageable pageable) {
        logger.debug("from {}, to {}", from, to);
        final DateTime fromDate = DateTime.parse(from);
        final DateTime toDate = DateTime.parse(to);
        final Page<PoliceTicker> between = policeTickerRepository.findByDatePublishedBetween(fromDate.toDateTimeISO()
                .toDate(), toDate.toDateTimeISO().toDate(), pageable);
        return between;
    }

    @RequestMapping(value = "/minmaxdate", method = RequestMethod.GET)
    @ResponseBody
    public DateTime[] minMaxDate() {
        final Page<PoliceTicker> minDate = policeTickerRepository.findAll(new PageRequest(0, 1, Direction.ASC,
                "datePublished"));
        final Page<PoliceTicker> maxDate = policeTickerRepository.findAll(new PageRequest(0, 1, Direction.DESC,
                "datePublished"));
        final DateTime minDatePublished = new DateTime(minDate.getContent().get(0).getDatePublished());
        final DateTime maxDatePublished = new DateTime(maxDate.getContent().get(0).getDatePublished());
        logger.debug("min {}, max {}", minDatePublished, maxDatePublished);
        return new DateTime[] { minDatePublished, maxDatePublished };
    }

    @RequestMapping(value = "/last7days", method = RequestMethod.GET)
    @ResponseBody
    public DateTime[] last7Days() {
        final DateTime now = DateTime.now();
        final DateTime minus7days = DateTime.now().minusDays(30);
        logger.debug("last7days: fromDate {}, toDate {}", minus7days, now);
        return new DateTime[] { minus7days, now };
    }

    private BoolQueryBuilder createFulltextSearchQueryBetween(Pageable pageable, List<String> splitToList, String from,
            String to) {
        BoolQueryBuilder searchQuery = null;
        if (splitToList.isEmpty()) {
            searchQuery = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery());
        } else {
            searchQuery = createFulltextSearchQueryBuilder(pageable, splitToList);
        }
        final DateTime fromDate = DateTime.parse(from);
        final DateTime toDate = DateTime.parse(to);

        final RangeQueryBuilder rqb = QueryBuilders.rangeQuery("datePublished").from(fromDate.toDateTimeISO().toDate())
                .to(toDate.toDateTimeISO().toDate());

        searchQuery.must(rqb);
        return searchQuery;
    }

    private BoolQueryBuilder createFulltextSearchQueryBuilder(Pageable pageable, List<String> splitToList) {
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
}
