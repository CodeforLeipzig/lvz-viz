package de.oklab.le;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.codefor.le.model.PoliceTicker;
import de.codefor.le.repositories.PoliceTickerRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/springContext-test.xml")
@Ignore
public class ReplacementTest {

    @Autowired
    private PoliceTickerRepository policeTickerRepository;

    @Before
    public void empty() {
        policeTickerRepository.deleteAll();
    }

    @Test
    public void test() {
        // String d = "2014-09-01T22:23:1.000Z";
        //
        // DateTime fromDate = DateTime.parse(d);
        // System.out.println(fromDate.toDate());

        DateTimeFormatter fmt = DateTimeFormat.forPattern("dd.MM.YYYY, HH:mm 'Uhr'");
        String dateToday = "10.08.2014, 09:06 Uhr";

        PoliceTicker pt = new PoliceTicker();
        pt.setDatePublished(DateTime.parse(dateToday, fmt).toDateTimeISO().toDate());
        pt.setArticle("This is the long article");
        pt.setArticleId("2341");
        pt.setSnippet("snippet at its best");

        policeTickerRepository.index(pt);

        pt = new PoliceTicker();
        String dateYesterday = "07.08.2014, 09:06 Uhr";
        pt.setDatePublished(DateTime.parse(dateYesterday, fmt).toDateTimeISO().toDate());
        pt.setArticle("This is the long article");
        pt.setArticleId("2341");
        pt.setSnippet("snippet at its best");

        policeTickerRepository.index(pt);

        fmt = DateTimeFormat.forPattern("EEE MMM dd yyyy HH:mm:ss zzzZ");
        // Tue Sep 02 2014 00:00:00 GMT+0200 (CEST)
        DateTime fromDate = DateTime.parse("Wed Jul 02 2014 00:00:00 GMT+0200", fmt);
        DateTime toDate = DateTime.parse("Fri Aug 08 2014 00:00:00 GMT+0200", fmt);
        System.out.println(toDate);
        System.out.println(fromDate.toDate());
        Page<PoliceTicker> findAll = policeTickerRepository.findAll(new PageRequest(0, 20));
        System.out.println(findAll.getContent());
        Page<PoliceTicker> res = policeTickerRepository.findByDatePublishedBetween(
                fromDate.toDate(), toDate.toDate(), new PageRequest(0, 20));
        System.out.println(res.getContent());

    }
}
