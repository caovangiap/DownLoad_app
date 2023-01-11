package com.muicvtools.mutils.downloads.pinterest;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PiPages {
    @SerializedName("blocks")
    private List<PiBlocks> blocks;

    public List<PiBlocks> getBlocks() {
        return blocks;
    }
}
