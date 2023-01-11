package com.muicvtools.mutils.downloads.insta.tv;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class InstaTV {
    @SerializedName("items")
    private List<Items> items;

    public List<Items> getItem() { return items; }
}
