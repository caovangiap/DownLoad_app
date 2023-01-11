package com.muicvtools.mutils.downloads.dailymt;

import com.google.gson.annotations.SerializedName;

public class DailymtAuto {
    @SerializedName("type")
    private String type;
    @SerializedName("url")
    private String url;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
