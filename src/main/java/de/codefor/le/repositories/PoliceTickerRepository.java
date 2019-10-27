package de.codefor.le.repositories;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import de.codefor.le.model.PoliceTicker;

@Repository
public interface PoliceTickerRepository extends ElasticsearchRepository<PoliceTicker, String> {

    Page<PoliceTicker> findByArticleContaining(String article, Pageable page);

    List<PoliceTicker> findByUrlIn(List<String> url);

    List<PoliceTicker> findByUrl(String url);

    List<PoliceTicker> findByTitle(String title);

    List<PoliceTicker> findByArticle(String article);

    Page<PoliceTicker> findByDatePublishedBetween(DateTime fromDate, DateTime toDate, Pageable page);
}
