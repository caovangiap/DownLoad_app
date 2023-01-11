package com.muicvtools.mutils.downloads.twitter;

import com.google.gson.annotations.SerializedName;

public class TwitterMedia {
    @SerializedName("media_url_https")
    private String thumb;

    @SerializedName("type")
    private String type;

    @SerializedName("video_info")
    private TwitterInfo video_info;

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public TwitterInfo getVideo_info() {
        return video_info;
    }

    public void setVideo_info(TwitterInfo video_info) {
        this.video_info = video_info;
    }
}
