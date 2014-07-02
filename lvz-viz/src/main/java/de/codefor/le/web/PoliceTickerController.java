package de.codefor.le.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    @ResponseBody
    public Iterable<PoliceTicker> findAll() {
        return policeTickerRepository.findAll();
    }

    @RequestMapping(value = "/extractlocations", method = RequestMethod.POST)
    @ResponseBody
    public Iterable<String> getLocations(@RequestBody String locations) {
        logger.info("{}", locations);
        return ner.getLocations(locations, false);
    }

    @RequestMapping(value = "/deleteAll", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteAll() {
        policeTickerRepository.deleteAll();
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ResponseBody
    public Iterable<PoliceTicker> search(@RequestParam String query) {
        return policeTickerRepository.findByArticleContaining(query);
    }
    
    
}
