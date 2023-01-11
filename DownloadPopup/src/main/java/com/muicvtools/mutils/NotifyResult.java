package com.muicvtools.mutils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NotifyResult {
    @SerializedName("notices")
    public List<com.muicvtools.mutils.Notify> notices;
}
