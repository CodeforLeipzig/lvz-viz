package de.oklab.le.LvzCrawler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * 
 * @author spinner0815
 * 
 */
public class LVBPoliceTickerCrawler extends Thread {

    private List<String> crawledNews = new ArrayList<>();
    private int maxPages;

    public LVBPoliceTickerCrawler(int maxPages) {
        this.maxPages = maxPages;
    }

    /**
     * 
     * @param page
     *            the page which to crawl
     * @return true if all content of the current page is new. Hint for also carwling the next site
     * @throws IOException
     *             if there are problems while writing the detail links to a file
     */
    public boolean crawlPage(int page) throws IOException {
        boolean result = false;

        // read everytime the file for getting all inserted links: already exists check
        Path DetailLinkPath = Paths.get(FileUtil.DETAIL_LINK_FILE);
        if (Files.exists(DetailLinkPath)) {
            crawledNews = Files.readAllLines(DetailLinkPath, Charset.forName("utf-8"));
        }
        Document doc = null;
        doc = Jsoup.connect(
                "http://www.lvz-online.de/leipzig/polizeiticker/polizeiticker-leipzig/r-polizeiticker-leipzig-seite-"
                        + page + ".html").get();
        List<String> detailLinks = new ArrayList<>();
        for (Element e : doc.select("a:contains(mehr...)")) {
            String detailLink = "http://www.lvz-online.de" + e.attr("href");
            if (!alreadyCrawled(detailLink)) {
                detailLinks.add(detailLink);
            }

        }
        Files.write(DetailLinkPath, detailLinks, Charset.forName("utf-8"),
                StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        if (detailLinks.size() == doc.select("a:contains(mehr...)").size()) {
            System.out.println("there is more to crawl!");
            result = true;
        }

        return result;
    }

    private boolean alreadyCrawled(String detailLink) throws IOException {
        boolean result = false;

        if (crawledNews.contains(detailLink)) {
            System.out.println(detailLink + " allready crawled");
            result = true;
        }
        return result;
    }

    @Override
    public void run() {
        int i = 1;
        try {
            while (crawlPage(i) && i < maxPages) {
                System.out.println("will wait");
                Thread.sleep(5000);
                System.out.println("waited");
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
