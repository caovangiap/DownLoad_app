package com.muicvtools.mutils;

import com.google.gson.annotations.SerializedName;

public class History {
    @SerializedName("title")
    public String title;
    @SerializedName("url")
    public String url;
    @SerializedName("type")
    public String type;

    @SerializedName("time")
    public com.muicvtools.mutils.GsonDate time;

    public History(String title, String url, String type, com.muicvtools.mutils.GsonDate time) {
        this.title = title;
        this.url = url;
        this.type = type;
        this.time = time;
    }
}
