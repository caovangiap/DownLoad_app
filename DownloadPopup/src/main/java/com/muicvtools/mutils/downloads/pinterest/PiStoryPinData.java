package com.muicvtools.mutils.downloads.pinterest;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PiStoryPinData {
    @SerializedName("pages")
    private List<PiPages> pages;

    public List<PiPages> getPages() {
        return pages;
    }
}
