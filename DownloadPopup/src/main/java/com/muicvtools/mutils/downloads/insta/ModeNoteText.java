package com.muicvtools.mutils.downloads.insta;

import com.google.gson.annotations.SerializedName;

public class ModeNoteText {
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @SerializedName("text")
    private String text;
}
