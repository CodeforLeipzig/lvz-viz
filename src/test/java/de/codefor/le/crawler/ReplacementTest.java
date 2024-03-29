package de.codefor.le.crawler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import de.codefor.le.model.PoliceTicker;
import de.codefor.le.repositories.PoliceTickerRepository;

@ActiveProfiles("test")
@SpringBootTest
class ReplacementTest {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    @Autowired
    private PoliceTickerRepository policeTickerRepository;

    @AfterEach
    public void empty() {
        policeTickerRepository.deleteAll();
    }

    @BeforeEach
    public void init() {
        var pt = new PoliceTicker();
        pt.setDatePublished(Date.from(LocalDateTime.of(2014, 8, 10, 9, 6).atZone(DEFAULT_ZONE).toInstant()));
        pt.setArticle("This is the long article");
        pt.setSnippet("snippet at its best");
        assertThat(policeTickerRepository.index(pt)).isNotNull();

        pt = new PoliceTicker();
        pt.setDatePublished(Date.from(LocalDateTime.of(2014, 8, 7, 9, 6).atZone(DEFAULT_ZONE).toInstant()));
        pt.setArticle("This is the long article");
        pt.setSnippet("snippet at its best");
        assertThat(policeTickerRepository.index(pt)).isNotNull();
    }

    @Test
    void findAll() {
        assertThat(policeTickerRepository.findAll(PageRequest.of(0, 20))).hasSize(2);
    }
}
