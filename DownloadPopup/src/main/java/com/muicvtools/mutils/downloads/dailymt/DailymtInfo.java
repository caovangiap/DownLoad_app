package com.muicvtools.mutils.downloads.dailymt;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class DailymtInfo {

    @SerializedName("title")
    private String title;

    @SerializedName("qualities")
    private DailymtQuality qualities;

    @SerializedName("posters")
    private Map<String,String> posters;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DailymtQuality getQualities() {
        return qualities;
    }

    public void setQualities(DailymtQuality qualities) {
        this.qualities = qualities;
    }

    public Map<String, String> getPosters() {
        return posters;
    }

    public void setPosters(Map<String, String> posters) {
        this.posters = posters;
    }
}
