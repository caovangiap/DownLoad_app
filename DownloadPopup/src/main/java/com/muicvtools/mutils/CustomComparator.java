package com.muicvtools.mutils;

import com.muicvtools.mutils.downloads.StreamQuality;
import com.muicvtools.mutils.downloads.VideoDetail;

import java.util.Comparator;

public class CustomComparator implements Comparator<VideoDetail> {
    @Override
    public int compare(VideoDetail o1, VideoDetail o2) {
        return (getValueQuality(o2.getQuality()) - getValueQuality(o1.getQuality()));
    }

    private int getValueQuality(StreamQuality quality)
    {
        switch (quality){
            case HD_8K:
                return 100;
            case HD_4K:
                return 99;
            case HD_2K:
                return 98;
            case HD_1080P:
                return 97;
            case HD_720P:
                return 96;
            case HD:
                return 80;
            case SD:
                return 70;
            case SD_640P:
                return 50;
            case SD_540P:
                return 49;
            case SD_480P:
                return 48;
            case SD_360P:
                return 47;
            case SD_240P:
                return 46;
            case SD_180P:
                return 45;

            default:
                return  0;
        }
    }
}