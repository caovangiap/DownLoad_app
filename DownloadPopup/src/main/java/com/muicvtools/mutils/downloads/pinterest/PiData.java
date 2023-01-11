package com.muicvtools.mutils.downloads.pinterest;

import com.google.gson.annotations.SerializedName;

public class PiData {
    @SerializedName("grid_title")
    private String grid_title;

    @SerializedName("videos")
    private PiVideos videos;

    @SerializedName("title")
    private String title;

    @SerializedName("story_pin_data")
    private PiStoryPinData story_pin_data;

    public String getGrid_title() {
        return grid_title;
    }

    public void setGrid_title(String grid_title) {
        this.grid_title = grid_title;
    }

    public PiVideos getVideos() {
        return videos;
    }

    public void setVideos(PiVideos videos) {
        this.videos = videos;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public PiStoryPinData getStory_pin_data() {
        return story_pin_data;
    }
}
