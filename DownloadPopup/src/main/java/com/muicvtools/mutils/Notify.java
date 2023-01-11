package com.muicvtools.mutils;

import com.google.gson.annotations.SerializedName;


public class Notify {
    @SerializedName("id")
    public int id;

    @SerializedName("title")
    public String title;
    @SerializedName("url")
    public String url;
    @SerializedName("msg")
    public String msg;
    @SerializedName("time")
    public GsonCompatibleDate time;
}
