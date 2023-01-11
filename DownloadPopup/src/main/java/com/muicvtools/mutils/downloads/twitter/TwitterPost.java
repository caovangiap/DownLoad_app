package com.muicvtools.mutils.downloads.twitter;

import com.google.gson.annotations.SerializedName;

public class TwitterPost {
    @SerializedName("full_text")
    private String title;

    @SerializedName("extended_entities")
    private Extended_entities extended_entities;

    @SerializedName("entities")
    private Entities entities;

    @SerializedName("user")
    private User user;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Extended_entities getExtended_entities() {
        return extended_entities;
    }

    public void setExtended_entities(Extended_entities extended_entities) {
        this.extended_entities = extended_entities;
    }

    public Entities getEntities() {
        return entities;
    }

    public void setEntities(Entities entities) {
        this.entities = entities;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
