package de.codefor.le.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.codefor.le.model.PoliceTicker;
import de.codefor.le.ner.NER;
import de.codefor.le.repositories.PoliceTickerRepository;

@Controller
public class PoliceTickerController {

    private static final Logger logger = LoggerFactory.getLogger(PoliceTickerController.class);
    @Autowired
    private PoliceTickerRepository policeTickerRepository;

    @Autowired
    private NER ner;

//    @RequestMapping(value = "/all", method = RequestMethod.GET)
//    @ResponseBody
//    public Iterable<PoliceTicker> findAll() {
//        return policeTickerRepository.findAll();
//    }

    @RequestMapping(value = "/getx", method = RequestMethod.GET)
    @ResponseBody
    public Iterable<PoliceTicker> getx(@RequestParam int page, @RequestParam int x) {
        return policeTickerRepository.findAll(new PageRequest(page, x));
    }

    @RequestMapping(value = "/extractlocations", method = RequestMethod.POST)
    @ResponseBody
    public Iterable<String> getLocations(@RequestBody String locations) {
        logger.info("{}", locations);
        return ner.getLocations(locations, false);
    }

    // @RequestMapping(value = "/deleteAll", method = RequestMethod.DELETE)
    // @ResponseBody
    // public void deleteAll() {
    // policeTickerRepository.deleteAll();
    // }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ResponseBody
    public Page<PoliceTicker> search(@RequestParam String query, @RequestParam int page, @RequestParam int limit) {
        return policeTickerRepository.findByArticleContaining(query, new PageRequest(page, limit));
    }

}
