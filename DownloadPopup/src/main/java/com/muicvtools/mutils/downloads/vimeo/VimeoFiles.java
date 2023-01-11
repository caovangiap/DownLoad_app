package com.muicvtools.mutils.downloads.vimeo;

import com.google.gson.annotations.SerializedName;

public class VimeoFiles {
    @SerializedName("progressive")
    private VimeoVideo [] progressive;

    public VimeoVideo[] getProgressive() {
        return progressive;
    }

    public void setProgressive(VimeoVideo[] progressive) {
        this.progressive = progressive;
    }
}
