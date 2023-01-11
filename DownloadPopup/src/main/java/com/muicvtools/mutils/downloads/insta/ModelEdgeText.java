package com.muicvtools.mutils.downloads.insta;

import com.google.gson.annotations.SerializedName;

public class ModelEdgeText {
    @SerializedName("node")
    private ModeNoteText modelNode;

    public ModeNoteText getModelNode() {
        return modelNode;
    }

    public void setModelNode(ModeNoteText modelNode) {
        this.modelNode = modelNode;
    }
}
