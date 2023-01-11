package com.muicvtools.mutils.downloads.twitter;

import com.google.gson.annotations.SerializedName;

public class TwitterInfo {

    @SerializedName("variants")
    private TwitterVariants [] variants;

    public TwitterVariants[] getVariants() {
        return variants;
    }

    public void setVariants(TwitterVariants[] variants) {
        this.variants = variants;
    }
}
