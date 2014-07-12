package de.oklab.le;

import org.junit.Test;

public class ReplacementTest {

    @Test
    public void test(){
        String url ="http://www.lvz-online.de/leipzig/polizeiticker/polizeiticker-leipzig/"
                + "mord-an-tagesmutter-in-leipzig-taeter-muss-lebenslang-ins-gefaengnis/r-polizeiticker-leipzig-a-246282.html";
        String u = url.replaceAll("/", "\\\\/");
        System.out.println(u);
    }
}
