package com.muicvtools.mutils.downloads.pinterest;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class PiVideos {

    @SerializedName("video_list")
    private Map<String,PiVideo> video_list;

    public Map<String, PiVideo> getVideo_list() {
        return video_list;
    }

    public void setVideo_list(Map<String, PiVideo> video_list) {
        this.video_list = video_list;
    }
}
