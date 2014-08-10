package de.oklab.le;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import de.codefor.le.repositories.PoliceTickerRepository;

@EnableAutoConfiguration
@EnableElasticsearchRepositories
public class Configuration {

}
