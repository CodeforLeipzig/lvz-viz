package de.codefor.le.crawler;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import de.codefor.le.crawler.model.Nominatim;

@Component
public class NominatimAsker extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(NominatimAsker.class);

    private RestTemplate restTemplate;
    private List<Nominatim> nominatim;
    private String adress;

    public NominatimAsker() {
        restTemplate = new RestTemplate();
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public List<Nominatim> getNominatim() {
        return nominatim;
    }

    @Override
    public void run() {
        try {
            getCoords();
            Thread.sleep(5000);
            logger.debug("finshed getting coords");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void getCoords() {
        String url = "http://nominatim.openstreetmap.org/search?q=" + adress + "&format=json";
        logger.debug("url {}", url);

        nominatim = Arrays.asList(restTemplate.getForObject(url, Nominatim[].class));
        logger.debug("p {}", nominatim.toString());
    }


}
