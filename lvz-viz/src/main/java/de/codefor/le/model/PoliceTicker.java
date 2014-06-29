package de.codefor.le.model;

import org.elasticsearch.common.geo.GeoPoint;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;

@Document(indexName = "policeticker-index", type = "policeticker-type", shards = 1, replicas = 0, refreshInterval = "-1")
@Mapping(mappingPath = "mapping.json")
public class PoliceTicker {

    @Id
    private String id;
    private String url;
    private String title;
    private String article;
    private String snippet;
    private String copyright;
    private String datePublished;
    private GeoPoint location;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public GeoPoint getCoords() {
        return location;
    }

    public void setCoords(GeoPoint location) {
        this.location = location;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((article == null) ? 0 : article.hashCode());
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((copyright == null) ? 0 : copyright.hashCode());
        result = prime * result + ((datePublished == null) ? 0 : datePublished.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        PoliceTicker other = (PoliceTicker) obj;
        if (article == null) {
            if (other.article != null)
                return false;
        } else if (!article.equals(other.article))
            return false;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
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
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
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
        builder.append("PoliceTicker [id=");
        builder.append(id);
        builder.append(", url=");
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
        builder.append(", coords=");
        builder.append(location);
        builder.append("]");
        return builder.toString();
    }

}
