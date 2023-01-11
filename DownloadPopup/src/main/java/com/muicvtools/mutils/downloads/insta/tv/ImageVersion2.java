package com.muicvtools.mutils.downloads.insta.tv;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ImageVersion2 {

    @SerializedName("candidates")
    private List<Candidates> candidates;

    public List<Candidates> getCandidates() {return candidates; }
}
