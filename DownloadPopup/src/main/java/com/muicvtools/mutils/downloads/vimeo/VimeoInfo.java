package com.muicvtools.mutils.downloads.vimeo;

import com.google.gson.annotations.SerializedName;

public class VimeoInfo {
    @SerializedName("title")
    private String title;

    @SerializedName("author_name")
    private String author_name;

    @SerializedName("thumbnail_url")
    private String thumbnail_url;

    @SerializedName("video_id")
    private String video_id;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public void setAuthor_name(String author_name) {
        this.author_name = author_name;
    }

    public String getThumbnail_url() {
        return thumbnail_url;
    }

    public void setThumbnail_url(String thumbnail_url) {
        this.thumbnail_url = thumbnail_url;
    }

    public String getVideo_id() {
        return video_id;
    }

    public void setVideo_id(String video_id) {
        this.video_id = video_id;
    }
}
