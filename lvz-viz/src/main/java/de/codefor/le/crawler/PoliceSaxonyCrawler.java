package de.codefor.le.crawler;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class PoliceSaxonyCrawler {

    // http://polizei.sachsen.de/de/medieninformationen_pdl.htm
    public static void main(String[] args) throws IOException {
//         Document doc =
//         Jsoup.connect("http://www.polizei.sachsen.de/de/presse_rss_pdl.xml").userAgent("leipzig crawler").get();
        Path path = FileSystems.getDefault().getPath("police.rss");
//        Files.write(path, doc.toString().getBytes(), StandardOpenOption.CREATE_NEW);
        byte[] content = Files.readAllBytes(path);
        System.out.println(new String(content));
        
//        Document parse = Jsoup.parse(new String(content));
        

    }
}
