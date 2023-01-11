package com.muicvtools.mutils.downloads.twitter;

import com.google.gson.annotations.SerializedName;

public class Entities {
    @SerializedName("urls")
    private VmapUrl[] urls;

    public VmapUrl[] getUrls() {
        return urls;
    }

    public void setUrls(VmapUrl[] urls) {
        this.urls = urls;
    }
}
