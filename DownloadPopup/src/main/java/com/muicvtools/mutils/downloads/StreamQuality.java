package com.muicvtools.mutils.downloads;

public enum StreamQuality
{
    HD,
    SD,
    HD_8K,
    HD_4K,
    HD_2K,
    HD_1080P,
    HD_720P,
    SD_360P,
    SD_540P,
    SD_480P,
    SD_640P,
    SD_240P,
    SD_180P,
    SD_144P,
    UNKOW,
    NO_WATERMARK,
    AUDIO;

    public boolean isHDvideo()
    {
        if(name().toString().startsWith("HD"))
            return true;
        else
            return false;
    }

}