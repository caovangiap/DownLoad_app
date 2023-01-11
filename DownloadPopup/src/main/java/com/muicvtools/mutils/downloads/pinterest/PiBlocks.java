package com.muicvtools.mutils.downloads.pinterest;

import com.google.gson.annotations.SerializedName;

public class PiBlocks {
    @SerializedName("video")
    private PinBlocksVideo video;

    public PinBlocksVideo getVideo() {
        return video;
    }
}
