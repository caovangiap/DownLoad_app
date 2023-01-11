package com.muicvtools.mutils.downloads;

import com.google.gson.annotations.SerializedName;

public class TokenResult {
    @SerializedName("guest_token")
    private String guest_token;

    public String getGuest_token() {
        return guest_token;
    }

    public void setGuest_token(String guest_token) {
        this.guest_token = guest_token;
    }
}
