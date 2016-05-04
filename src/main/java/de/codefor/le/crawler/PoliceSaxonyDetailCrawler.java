package de.codefor.le.crawler;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PoliceSaxonyDetailCrawler {

    private static final Logger logger = LoggerFactory.getLogger(PoliceSaxonyDetailCrawler.class);

    public static void main(String[] args) throws IOException {
        Path path = FileSystems.getDefault().getPath("policedetail.html");
        byte[] content = Files.readAllBytes(path);

        Document doc = Jsoup.parse(new String(content));
        Elements select = doc.select("p.clear + div");
        for (Element e : select) {
            if (!e.getElementsByTag("strong").isEmpty()) {
                Elements eStrong = e.getElementsByTag("strong");
                for (Element eu : eStrong) {
                    logger.info("STRONG {}", eu.ownText());
                }
            } else {
                logger.info("NORMAL {}", e);
            }
        }
    }
}
