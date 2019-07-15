package de.codefor.le.crawler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.codefor.le.model.PoliceTicker;
import de.codefor.le.repositories.PoliceTickerRepository;

@Disabled
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ReplacementTest {

    @Autowired
    private PoliceTickerRepository policeTickerRepository;

    @BeforeEach
    public void empty() {
        policeTickerRepository.deleteAll();
    }

    @Test
    public void test() {
        PoliceTicker pt = new PoliceTicker();
        pt.setDatePublished(Date.from(LocalDateTime.of(2014, 8, 10, 9, 6).atZone(ZoneId.systemDefault()).toInstant()));
        pt.setArticle("This is the long article");
        pt.setSnippet("snippet at its best");

        policeTickerRepository.index(pt);

        pt = new PoliceTicker();
        pt.setDatePublished(Date.from(LocalDateTime.of(2014, 8, 7, 9, 6).atZone(ZoneId.systemDefault()).toInstant()));
        pt.setArticle("This is the long article");
        pt.setSnippet("snippet at its best");

        policeTickerRepository.index(pt);

        final Date fromDate = Date.from(LocalDateTime.of(2014, 7, 2, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        final Date toDate = Date.from(LocalDateTime.of(2014, 8, 8, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        final Page<PoliceTicker> findAll = policeTickerRepository.findAll(new PageRequest(0, 20));
        System.out.println(findAll.getContent());
        final Page<PoliceTicker> res = policeTickerRepository.findByDatePublishedBetween(fromDate, toDate, new PageRequest(0, 20));
        System.out.println(res.getContent());

    }
}
