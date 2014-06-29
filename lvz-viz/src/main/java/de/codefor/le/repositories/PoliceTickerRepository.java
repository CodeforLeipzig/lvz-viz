package de.codefor.le.repositories;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import de.codefor.le.model.PoliceTicker;

public interface PoliceTickerRepository extends ElasticsearchRepository<PoliceTicker, Long> {

    List<PoliceTicker> findByArticleContaining(String article);

    List<PoliceTicker> findByUrlIn(List<String> url);
}
