package de.codefor.le.crawler.model;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Nominatim {

    private String place_id;
    private String[] boundingbox;
    private String lat;
    private String lon;
    private String type;

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String[] getBoundingbox() {
        return boundingbox;
    }

    public void setBoundingbox(String[] boundingbox) {
        this.boundingbox = boundingbox;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(boundingbox);
        result = prime * result + ((lat == null) ? 0 : lat.hashCode());
        result = prime * result + ((lon == null) ? 0 : lon.hashCode());
        result = prime * result + ((place_id == null) ? 0 : place_id.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        Nominatim other = (Nominatim) obj;
        if (!Arrays.equals(boundingbox, other.boundingbox))
            return false;
        if (lat == null) {
            if (other.lat != null)
                return false;
        } else if (!lat.equals(other.lat))
            return false;
        if (lon == null) {
            if (other.lon != null)
                return false;
        } else if (!lon.equals(other.lon))
            return false;
        if (place_id == null) {
            if (other.place_id != null)
                return false;
        } else if (!place_id.equals(other.place_id))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Nominatim [place_id=");
        builder.append(place_id);
        builder.append(", boundingbox=");
        builder.append(Arrays.toString(boundingbox));
        builder.append(", lat=");
        builder.append(lat);
        builder.append(", lon=");
        builder.append(lon);
        builder.append(", type=");
        builder.append(type);
        builder.append("]");
        return builder.toString();
    }

}
