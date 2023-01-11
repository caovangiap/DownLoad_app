package com.muicvtools.mutils.downloads.insta;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ModeEdgeMediaToCaption
{
    @SerializedName("edges")
    private List<ModelEdgeText> modelEdges;

    public List<ModelEdgeText> getModelEdges() {
        return modelEdges;
    }

    public void setModelEdges(List<ModelEdgeText> modelEdges) {
        this.modelEdges = modelEdges;
    }
}
