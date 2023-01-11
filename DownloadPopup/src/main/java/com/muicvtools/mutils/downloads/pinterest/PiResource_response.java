package com.muicvtools.mutils.downloads.pinterest;

import com.google.gson.annotations.SerializedName;

public class PiResource_response {
    @SerializedName("data")
    private PiData data;

    public PiData getData() {
        return data;
    }

    public void setData(PiData data) {
        this.data = data;
    }
}
