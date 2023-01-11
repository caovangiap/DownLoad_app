package com.muicvtools.mutils.downloads.pinterest;

import com.google.gson.annotations.SerializedName;

public class PiVideo {
    @SerializedName("url")
    private String url;
    @SerializedName("thumbnail")
    private String thumbnail;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
