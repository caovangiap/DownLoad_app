package com.muicvtools.mutils.downloads.twitter;

import com.google.gson.annotations.SerializedName;

public class Extended_entities {
    @SerializedName("media")
    private TwitterMedia [] media;

    public TwitterMedia[] getMedia() {
        return media;
    }

    public void setMedia(TwitterMedia[] media) {
        this.media = media;
    }
}
