package de.codefor.le.model;

import java.util.Date;

import org.elasticsearch.common.geo.GeoPoint;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;

import lombok.Data;

@Data
@Document(indexName = "policeticker-index", type = "policeticker-type", shards = 1, replicas = 0, refreshInterval = "-1")
@Mapping(mappingPath = "mapping.json")
public class PoliceTicker {

    @Id
    private String id;

    private String url;

    private String title;

    private String article;

    private String snippet;

    private String copyright;

    private Date datePublished;

    private GeoPoint coords;

}
