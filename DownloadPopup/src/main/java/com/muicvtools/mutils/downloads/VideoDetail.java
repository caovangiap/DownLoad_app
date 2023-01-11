package com.muicvtools.mutils.downloads;

public class VideoDetail {
    private String url_stream;
    private StreamQuality quality;
    private String url_thumb;

    public String getUrl_thumb() {
        return url_thumb;
    }

    public void setUrl_thumb(String url_thumb) {
        this.url_thumb = url_thumb;
    }

    public VideoDetail(String url_stream, StreamQuality quality, String url_thumb) {
        this.url_stream = url_stream;
        this.quality = quality;
        this.url_thumb = url_thumb;
    }

    public String getUrl_stream() {
        return url_stream;
    }

    public void setUrl_stream(String url_stream) {
        this.url_stream = url_stream;
    }

    public StreamQuality getQuality() {
        return quality;
    }

    public void setQuality(StreamQuality quality) {
        this.quality = quality;
    }
}
