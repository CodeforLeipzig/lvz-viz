package de.oklab.le.LvzCrawler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {   
        Document doc=null;
        try {
            doc = Jsoup.connect("http://www.lvz-online.de/leipzig/polizeiticker/polizeiticker-leipzig/r-polizeiticker-leipzig-seite-1.html").get();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int i =0;
        for(Element e : doc.select("a:contains(mehr...)")){
            
            String link = "http://www.lvz-online.de"+e.attr("href");
            try {
                Document d = Jsoup.connect(link).get();
                Files.write(Paths.get("/data/programming/codeforleipzig/"+i+".html"), d.toString().getBytes());
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            i++;
            System.out.println(link);
        }
        System.out.println();
//        Elements newsHeadlines = doc.select("#mp-itn b a");
//        System.out.println( doc.toString() );
    }
}
