package com.muicvtools.mutils.downloads;

import com.google.gson.annotations.SerializedName;

public class ScriptInfo {
    @SerializedName("version")
    public int version;
    @SerializedName("url_web")
    public String url_web;
    @SerializedName("js_script")
    public String js_script;
    @SerializedName("is_active")
    public boolean is_active;

    public SCRIPT_TYPE script_type;

    public ScriptInfo(String url_web,String js_script,SCRIPT_TYPE script_type)
    {
        this.url_web = url_web;
        this.js_script = js_script;
        this.is_active = true;
        this.version = 0;
        this.script_type = script_type;
    }

}
