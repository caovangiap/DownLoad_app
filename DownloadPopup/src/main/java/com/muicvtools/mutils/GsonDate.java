package com.muicvtools.mutils;

import com.google.gson.annotations.SerializedName;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GsonDate {
    @SerializedName("$date")
    public long time;

    public GsonDate(long time)
    {
        this.time = time;
    }

    public String getDateString()
    {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("MM/dd/yyyy");
        return format.format(date);
    }
}
