package com.muicvtools.mutils.downloads.vimeo;

import com.google.gson.annotations.SerializedName;

public class VimeoRequest {
    @SerializedName("files")
    private VimeoFiles files;

    public VimeoFiles getFiles() {
        return files;
    }

    public void setFiles(VimeoFiles files) {
        this.files = files;
    }
}
