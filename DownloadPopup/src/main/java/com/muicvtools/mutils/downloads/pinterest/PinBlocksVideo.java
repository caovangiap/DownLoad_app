package com.muicvtools.mutils.downloads.pinterest;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class PinBlocksVideo {
    @SerializedName("video_list")
    private Map<String,PiVideo> video_list;

    public Map<String, PiVideo> getVideo_list() {
        return video_list;
    }
}
