package com.muicvtools.mutils.downloads;

import com.muicvtools.mutils.CustomComparator;

import org.schabi.newpipe.extractor.stream.StreamInfo;

import java.util.ArrayList;
import java.util.Collections;

public class StreamOtherInfo {
    private String url_source;
    private DownloadType url_type;
    private ArrayList<VideoDetail> urls_stream;
    private String title_file;
    private String url_thumb;

    private StreamInfo ytStreamInfo;

    public StreamOtherInfo(String url_source, DownloadType url_type, ArrayList<VideoDetail> urls_stream, String title_file,String url_thumb) {
        this.url_source = url_source;
        this.url_type = url_type;
        this.urls_stream = urls_stream;
        Collections.sort(this.urls_stream,new CustomComparator());
        this.title_file = title_file;
        this.url_thumb = url_thumb;
    }

    public StreamOtherInfo(String url_source,DownloadType url_type,StreamInfo ytStreamInfo,ArrayList<VideoDetail> urls_stream, String title_file,String url_thumb)
    {
        this.url_source = url_source;
        this.url_type = url_type;
        this.ytStreamInfo = ytStreamInfo;
        this.urls_stream = urls_stream;
        this.title_file = title_file;
        this.url_thumb = url_thumb;
    }

    public String getUrl_thumb() {
        return url_thumb;
    }

    public void setUrl_thumb(String url_thumb) {
        this.url_thumb = url_thumb;
    }

    public String getUrl_source() {
        return url_source;
    }

    public void setUrl_source(String url_source) {
        this.url_source = url_source;
    }

    public DownloadType getUrl_type() {
        return url_type;
    }

    public void setUrl_type(DownloadType url_type) {
        this.url_type = url_type;
    }

    public ArrayList<VideoDetail> getUrls_stream() {
        return urls_stream;
    }

    public void setUrls_stream(ArrayList<VideoDetail> urls_stream) {
        this.urls_stream = urls_stream;
    }

    public String getTitle_file() {
        return title_file;
    }

    public void setTitle_file(String title_file) {
        this.title_file = title_file;
    }

    public StreamInfo getYtStreamInfo() {
        return ytStreamInfo;
    }

    public void setYtStreamInfo(StreamInfo ytStreamInfo) {
        this.ytStreamInfo = ytStreamInfo;
    }
}








