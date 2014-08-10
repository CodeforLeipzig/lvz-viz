package de.codefor.le;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@EnableAsync
@EnableScheduling
@EnableSpringDataWebSupport
public class LvzViz {

    public static void main(String[] args) {
        SpringApplication.run(LvzViz.class, args);
    }

}
