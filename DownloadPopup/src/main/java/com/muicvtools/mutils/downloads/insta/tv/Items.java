package com.muicvtools.mutils.downloads.insta.tv;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Items {
    @SerializedName("caption")
    private Caption caption;

    @SerializedName("image_versions2")
    private ImageVersion2 image_version2;

    @SerializedName("video_versions")
    private List<VideoVersions> video_versions;

    public Caption getCaption() {
        return caption;
    }

    public ImageVersion2 getImageVersion2() { return  image_version2; }

    public List<VideoVersions> getVideoVersions() {
        return video_versions;
    }
}
