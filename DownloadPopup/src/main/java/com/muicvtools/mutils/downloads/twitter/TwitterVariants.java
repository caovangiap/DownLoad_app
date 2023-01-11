package com.muicvtools.mutils.downloads.twitter;

import com.google.gson.annotations.SerializedName;

public class TwitterVariants {
    @SerializedName("bitrate")
    private long bitrate;

    @SerializedName("content_type")
    private String content_type;

    @SerializedName("url")
    private String url;

    public long getBitrate() {
        return bitrate;
    }

    public void setBitrate(long bitrate) {
        this.bitrate = bitrate;
    }

    public String getContent_type() {
        return content_type;
    }

    public void setContent_type(String content_type) {
        this.content_type = content_type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
