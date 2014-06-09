package de.oklab.le.LvzCrawler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import de.oklab.le.LvzCrawler.model.DetailModel;

/**
 * TODO - create a thread which sleeps 5sec after a page was crawled
 * 
 * crawls the detailview to extract and store teh following information into a csv file
 * 
 * <ul>
 * <li>title
 * <li>url
 * <li>article
 * <li>snippet (20words and three points)
 * <li>copyright
 * <li>date published
 * </ul>
 * 
 * @author spinner0815
 */
public class LVBPoliceTickerDetailViewCrawler {

    // link \t ueberschrift \t inhalt_plain \t copyright \t Datum der Veroeffentlichung
    public void crawl(String path) {
        DetailModel result = new DetailModel();
        Document doc = null;
        try {
            doc = Jsoup.connect(path).get();
            if (doc != null) {
                result = convertToDataModel(doc);
                result.setUrl(path);
                debugPrint(result);
            }
            storeToCsv(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * mapper to map the information of the document into a model
     * 
     * @param path
     *            the url
     * @param doc
     *            the document
     * @return the model with all infromation which are needed
     */
    private DetailModel convertToDataModel(Document doc) {
        DetailModel dm = new DetailModel();
        extractTitle(doc, dm);
        extractArticleAndsnippet(doc, dm);
        extractCopyright(doc, dm);
        extractDatePublished(doc, dm);
        return dm;
    }

    /**
     * prints the details of the detail view
     * 
     * @param dm
     */
    private void debugPrint(DetailModel dm) {
        System.out.println("Article: " + dm.getArticle());
        System.out.println("Copyright: " + dm.getCopyright());
        System.out.println("Published: " + dm.getDatePublished());
        System.out.println("Snippet: " + dm.getSnippet());
        System.out.println("Title: " + dm.getTitle());
        System.out.println("URL: " + dm.getUrl());
    }

    /**
     * extracts the title from the document
     * 
     * @param doc
     *            the document with the article
     * @param dm
     *            the datamodel for the information which are needed
     */
    private void extractTitle(Document doc, DetailModel dm) {
        dm.setTitle(doc.select("title").first().ownText());
    }

    /**
     * extracts the copyright
     * 
     * @param doc
     * @param dm
     */
    private void extractCopyright(Document doc, DetailModel dm) {
        String copyrightAndDatePublished = extractCopyrightAndDatePublished(doc);
        dm.setCopyright(copyrightAndDatePublished.split(",")[0]);
        ;
    }

    private void extractDatePublished(Document doc, DetailModel dm) {
        String copyrightAndDatePublished = extractCopyrightAndDatePublished(doc);
        dm.setDatePublished(copyrightAndDatePublished.substring(copyrightAndDatePublished.indexOf(",") + 1).trim());

    }

    private String extractCopyrightAndDatePublished(Document doc) {
        String result = "";
        for (Element e : doc.select("div.copyright")) {
            // only plain copyright
            if (e.hasText()) {
                result = e.ownText();
            }
        }
        return result;
    }

    private void extractArticleAndsnippet(Document doc, DetailModel dm) {
        for (Element e : doc.select("div.ARTIKEL_TEXT")) {
            if (e.hasText()) {
                String article = e.ownText();
                String[] split = article.split("\\s");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 20; i++) {
                    sb.append(split[i]).append(" ");
                }
                String abstractDesc = sb.toString().trim() + "...";

                dm.setArticle(article);
                dm.setSnippet(abstractDesc);
            }
        }
    }

    private void storeToCsv(DetailModel dm) throws IOException {
        Path filePath = Paths.get(FileUtil.DETAIL_CONTENT_FILE);
        if (!Files.exists(filePath)) {
            Files.write(filePath, "url\ttitle\tarticle\tsnippet\tpublished\tcopyright\n".getBytes(),
                    StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(dm.getUrl()).append("\t");// url
        sb.append(dm.getTitle()).append("\t");// title
        sb.append(dm.getArticle()).append("\t");// article
        sb.append(dm.getSnippet()).append("\t");// snippet
        sb.append(dm.getDatePublished()).append("\t");// published
        sb.append(dm.getCopyright()).append("\t");// copyright
        sb.append("\n");// new line

        Files.write(filePath, sb.toString().getBytes(), StandardOpenOption.APPEND);
    }
}
