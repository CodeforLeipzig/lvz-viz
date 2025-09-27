package de.codefor.le.crawler.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Nominatim {

    private String place_id;

    private String[] boundingbox;

    private String lat;

    private String lon;

    private String type;

}
