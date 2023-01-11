package com.muicvtools.mutils.downloads.twitter;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("profile_image_url_https")
    private String profile_image_url_https;

    public String getProfile_image_url_https() {
        return profile_image_url_https;
    }

    public void setProfile_image_url_https(String profile_image_url_https) {
        this.profile_image_url_https = profile_image_url_https;
    }
}
