package de.codefor.le.repositories;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import de.codefor.le.model.PoliceTicker;

@Repository
public interface PoliceTickerRepository extends ElasticsearchRepository<PoliceTicker, String> {
}
