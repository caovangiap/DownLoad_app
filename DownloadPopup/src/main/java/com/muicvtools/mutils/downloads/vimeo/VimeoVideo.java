package com.muicvtools.mutils.downloads.vimeo;

import com.google.gson.annotations.SerializedName;

public class VimeoVideo {
    @SerializedName("mime")
    private String mime;

    @SerializedName("url")
    private String url;

    @SerializedName("quality")
    private String quality;

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }
}
