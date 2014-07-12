package de.codefor.le.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import de.codefor.le.model.PoliceTicker;

public interface PoliceTickerRepository extends ElasticsearchRepository<PoliceTicker, Long> {

    Page<PoliceTicker> findByArticleContaining(String article, Pageable page);

    List<PoliceTicker> findByUrlIn(List<String> url);

    List<PoliceTicker> findByUrl(String url);

    List<PoliceTicker> findByTitle(String title);

    List<PoliceTicker> findByArticle(String article);
}
