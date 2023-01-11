package com.muicvtools.mutils.downloads.vimeo;

import com.google.gson.annotations.SerializedName;

public class VimeoConfig {
    @SerializedName("request")
    private VimeoRequest request;

    public VimeoRequest getRequest() {
        return request;
    }

    public void setRequest(VimeoRequest request) {
        this.request = request;
    }
}
