package de.oklab.le.LvzCrawler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author spinner0815
 * 
 */
public class App {
    public static void main(String[] args) {
        LVBPoliceTickerCrawler tickerCrawler = new LVBPoliceTickerCrawler(3);
        tickerCrawler.start();
        while (tickerCrawler.isAlive()) {

        }
        List<String> articleUrls = new ArrayList<>();
        try {
            articleUrls = Files.readAllLines(Paths.get(FileUtil.DETAIL_LINK_FILE), Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        LVBPoliceTickerDetailViewCrawler articleCrawler = new LVBPoliceTickerDetailViewCrawler();
        for (String url : articleUrls) {
            articleCrawler.crawl(url);
        }

    }

}
