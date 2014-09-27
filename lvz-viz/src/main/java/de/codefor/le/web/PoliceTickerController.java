package de.codefor.le.web;

import java.util.List;

import org.apache.lucene.queryparser.surround.query.OrQuery;
import org.apache.lucene.search.TermQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
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
import com.google.common.collect.Lists;

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

    @Autowired
    private NER ner;

    @RequestMapping(value = "/getx", method = RequestMethod.GET)
    @ResponseBody
    public Iterable<PoliceTicker> getx(@PageableDefault Pageable pageable) {
        return policeTickerRepository.findAll(pageable);
    }

    @RequestMapping(value = "/extractlocations", method = RequestMethod.POST)
    @ResponseBody
    public Iterable<String> getLocations(@RequestBody String locations) {
        logger.info("{}", locations);
        return ner.getLocations(locations, false);
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ResponseBody
    public Page<PoliceTicker> search(@RequestParam String query,
            @PageableDefault(direction = Direction.DESC, sort = "datePublished") Pageable pageable) {
        logger.info("query: {}", query);
        query = query.toLowerCase();
        // QueryStringQueryBuilder qsqb = new QueryStringQueryBuilder(query).field("title").field("article");
        List<String> splitToList = Splitter.on(CharMatcher.WHITESPACE).splitToList(query);

        SearchQuery sq = createFulltextSearchQuery(pageable, splitToList);
        elasticsearchTemplate.queryForPage(sq, PoliceTicker.class);
        return elasticsearchTemplate.queryForPage(sq, PoliceTicker.class);
    }

    @RequestMapping(value = "/between", method = RequestMethod.GET)
    @ResponseBody
    public Page<PoliceTicker> between(@RequestParam String from, @RequestParam String to,
            @PageableDefault(direction = Direction.DESC, sort = "datePublished", page = 0, size = 40) Pageable pageable) {
        logger.info("from {}, to {}", from, to);
        DateTime fromDate = DateTime.parse(from);
        DateTime toDate = DateTime.parse(to);
        Page<PoliceTicker> between = policeTickerRepository.findByDatePublishedBetween(
                fromDate.toDateTimeISO().toDate(), toDate.toDateTimeISO().toDate(), pageable);
        return between;
    }

    @RequestMapping(value = "/minmaxdate", method = RequestMethod.GET)
    @ResponseBody
    public DateTime[] minMaxDate() {
        Page<PoliceTicker> minDate = policeTickerRepository.findAll(new PageRequest(0, 1, Direction.ASC,
                "datePublished"));
        Page<PoliceTicker> maxDate = policeTickerRepository.findAll(new PageRequest(0, 1, Direction.DESC,
                "datePublished"));
        DateTime minDatePublished = new DateTime(minDate.getContent().get(0).getDatePublished());
        DateTime maxDatePublished = new DateTime(maxDate.getContent().get(0).getDatePublished());
        logger.info("min {}, max {}", minDatePublished, maxDatePublished);
        return new DateTime[] { minDatePublished, maxDatePublished };
    }

    @RequestMapping(value = "/last7days", method = RequestMethod.GET)
    @ResponseBody
    public DateTime[] last7Days() {
        logger.info("last7days");

        DateTime now = DateTime.now();
        DateTime minus7days = DateTime.now().minusDays(7);
        logger.info("last7days: fromDate {}, toDate {}", minus7days, now);
        return new DateTime[] { minus7days, now };
    }

    private SearchQuery createFulltextSearchQuery(Pageable pageable, List<String> splitToList) {
        BoolQueryBuilder articleBool = QueryBuilders.boolQuery();
        BoolQueryBuilder titleBool = QueryBuilders.boolQuery();
        for (String s : splitToList) {
            articleBool.must(QueryBuilders.termQuery("article", s));
            titleBool.must(QueryBuilders.termQuery("title", s));

        }
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.should(articleBool);
        boolQueryBuilder.should(titleBool);
        SearchQuery sq = new NativeSearchQueryBuilder().withPageable(pageable)
                .withQuery(boolQueryBuilder).build();
        logger.info("{}", boolQueryBuilder);
        return sq;
    }
}
