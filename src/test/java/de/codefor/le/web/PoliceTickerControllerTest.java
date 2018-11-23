package de.codefor.le.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import de.codefor.le.model.PoliceTicker;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PoliceTickerControllerTest {

    @Autowired
    PoliceTickerController controller;

    @Test
    public void getx() {
        final Page<PoliceTicker> result = controller.getx(new PageRequest(0, 1));
        assertNotNull(result);
        assertEquals(1, result.getSize());
    }

    @Test
    public void getLocations() {
        final Iterable<String> result = controller.getLocations("Lindenau");
        assertNotNull(result);
        assertFalse(result.iterator().hasNext());
    }

    @Test
    public void search() {
        final Page<PoliceTicker> result = controller.search("term", new PageRequest(0, 1));
        assertNotNull(result);
        assertEquals(0, result.getNumberOfElements());
    }

    @Test
    public void searchBetween() {
        final Page<PoliceTicker> result = controller.searchBetween("term",
                LocalDateTime.now().minus(1, ChronoUnit.DAYS), LocalDateTime.now(), new PageRequest(0, 1));
        assertNotNull(result);
        assertEquals(0, result.getNumberOfElements());
    }

    @Test
    public void minMaxDate() {
        final DateTime[] result = controller.minMaxDate();
        assertNotNull(result);
        assertEquals(2, result.length);
    }

    @Test
    public void last7Days() {
        final DateTime[] result = controller.last7Days();
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals(DateTime.now().getDayOfMonth(), result[1].getDayOfMonth());
    }
}
