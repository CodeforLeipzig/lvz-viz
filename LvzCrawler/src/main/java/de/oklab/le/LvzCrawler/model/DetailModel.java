package de.oklab.le.LvzCrawler.model;

import java.util.Date;

public class DetailModel {

    private String url;
    private String title;
    private String article;
    private String snippet;
    private String copyright;
    private String datePublished;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getDatePublished() {
        return datePublished;
    }

    public void setDatePublished(String datePublished) {
        this.datePublished = datePublished;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((article == null) ? 0 : article.hashCode());
        result = prime * result + ((copyright == null) ? 0 : copyright.hashCode());
        result = prime * result + ((datePublished == null) ? 0 : datePublished.hashCode());
        result = prime * result + ((snippet == null) ? 0 : snippet.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DetailModel other = (DetailModel) obj;
        if (article == null) {
            if (other.article != null)
                return false;
        } else if (!article.equals(other.article))
            return false;
        if (copyright == null) {
            if (other.copyright != null)
                return false;
        } else if (!copyright.equals(other.copyright))
            return false;
        if (datePublished == null) {
            if (other.datePublished != null)
                return false;
        } else if (!datePublished.equals(other.datePublished))
            return false;
        if (snippet == null) {
            if (other.snippet != null)
                return false;
        } else if (!snippet.equals(other.snippet))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        if (url == null) {
            if (other.url != null)
                return false;
        } else if (!url.equals(other.url))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DetailModel [url=");
        builder.append(url);
        builder.append(", title=");
        builder.append(title);
        builder.append(", article=");
        builder.append(article);
        builder.append(", snippet=");
        builder.append(snippet);
        builder.append(", copyright=");
        builder.append(copyright);
        builder.append(", datePublished=");
        builder.append(datePublished);
        builder.append("]");
        return builder.toString();
    }

}
