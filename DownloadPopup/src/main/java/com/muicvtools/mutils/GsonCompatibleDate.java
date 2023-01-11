package com.muicvtools.mutils;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class GsonCompatibleDate {
    @SerializedName("$date")
    public Long date;

    public Date getDate() {
        if (date == null)
            return new Date();
        else
            return new Date(date);
    }
}