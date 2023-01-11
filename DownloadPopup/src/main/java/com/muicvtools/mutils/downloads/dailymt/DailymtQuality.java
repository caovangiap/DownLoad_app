package com.muicvtools.mutils.downloads.dailymt;

import com.google.gson.annotations.SerializedName;

public class DailymtQuality {
    @SerializedName("auto")
    private DailymtAuto [] auto;

    public DailymtAuto[] getAuto() {
        return auto;
    }

    public void setAuto(DailymtAuto[] auto) {
        this.auto = auto;
    }
}
