package de.codefor.le;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@EnableSpringDataWebSupport
@SpringBootApplication
public class LvzViz {

    public static void main(final String[] args) {
        SpringApplication.run(LvzViz.class, args);
    }

}
