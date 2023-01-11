package com.muicvtools.mutils.downloads.pinterest;

import com.google.gson.annotations.SerializedName;

public class PiPin {
    @SerializedName("resource_response")
    private PiResource_response resource_response;

    public PiResource_response getResource_response() {
        return resource_response;
    }

    public void setResource_response(PiResource_response resource_response) {
        this.resource_response = resource_response;
    }
}
