package com.muicvtools.mutils.downloads.insta;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ModelShortcodeMedia implements Serializable {

    @SerializedName("display_url")
    private String display_url;

    @SerializedName("edge_media_to_caption")
    private ModeEdgeMediaToCaption edge_media_to_caption;

    @SerializedName("is_video")
    private boolean is_video;

    @SerializedName("video_url")
    private String video_url;

    @SerializedName("title")
    private String title;

    @SerializedName("edge_sidecar_to_children")
    private ModelEdgeSidecarToChildren edge_sidecar_to_children;


    public String getDisplay_url() {
        return display_url;
    }

    public void setDisplay_url(String display_url) {
        this.display_url = display_url;
    }

    public ModeEdgeMediaToCaption getEdge_media_to_caption() {
        return edge_media_to_caption;
    }

    public void setEdge_media_to_caption(ModeEdgeMediaToCaption edge_media_to_caption) {
        this.edge_media_to_caption = edge_media_to_caption;
    }

    public boolean isIs_video() {
        return is_video;
    }

    public void setIs_video(boolean is_video) {
        this.is_video = is_video;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public ModelEdgeSidecarToChildren getEdge_sidecar_to_children() {
        return edge_sidecar_to_children;
    }

    public void setEdge_sidecar_to_children(ModelEdgeSidecarToChildren edge_sidecar_to_children) {
        this.edge_sidecar_to_children = edge_sidecar_to_children;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    //    public String getAccessibility_caption() {
//        return accessibility_caption;
//    }
//
//    public void setAccessibility_caption(String accessibility_caption) {
//        this.accessibility_caption = accessibility_caption;
//    }
}
