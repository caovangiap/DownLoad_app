package com.muicvtools.mutils.downloads.twitter;

import com.google.gson.annotations.SerializedName;

public class VmapUrl {
    @SerializedName("expanded_url")
    private String expanded_url;

    public String getExpanded_url() {
        return expanded_url;
    }

    public void setExpanded_url(String expanded_url) {
        this.expanded_url = expanded_url;
    }
}
