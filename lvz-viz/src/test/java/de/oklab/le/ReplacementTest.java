package de.oklab.le;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class ReplacementTest {

    @Test
    public void test() {
        String url = "http://www.lvz-online.de/leipzig/polizeiticker/polizeiticker-leipzig/"
                + "mord-an-tagesmutter-in-leipzig-taeter-muss-lebenslang-ins-gefaengnis/r-polizeiticker-leipzig-a-246282.html";
        String u = url.replaceAll("/", "\\\\/");
        System.out.println(u);
    }

    @Test
    public void articleIdExtracting(){
        String url = "www.lvz-online.de/leipzig/polizeiticker/polizeiticker-leipzig/mord-an-tagesmutter-in-leipzig-taeter-muss-lebenslang-ins-gefaengnis/r-polizeiticker-leipzig-a-246282.html";
        int lastSlash = url.lastIndexOf("/");
        String lastOne = url.substring(lastSlash);
        System.out.println("last one "+ lastOne);
        Pattern p = Pattern.compile("[0-9]{6}");
        Matcher m = p.matcher(lastOne);
        if(m.find()){
        String articleId = lastOne.substring(m.start(), m.end());
        Assert.assertEquals("246282", articleId);
        }
        
    }
}
