package com.muicvtools.mutils;

import com.google.gson.annotations.SerializedName;

public class ModelData {
    @SerializedName("id")
    public String id;
    @SerializedName("is_accept")
    public boolean is_accept;
    @SerializedName("is_premium")
    public boolean is_premium;
    @SerializedName("current_build")
    public String current_build;
    @SerializedName("delay_show_full")
    public int delay_show_full;
    @SerializedName("delay_show_reward")
    public int delay_show_reward;
    @SerializedName("delay_show_openads")
    public int delay_show_openads;
    @SerializedName("is_ads_watch_video")
    public boolean is_ads_watch_video;
    @SerializedName("banner_ad_id")
    public String id_banner_ad;
    @SerializedName("banner_home_ad_id")
    public String id_banner_ad_home;
    @SerializedName("full_ad_id")
    public String id_full_ad;
    @SerializedName("reward_ad_id")
    public String id_reward_ad;
    @SerializedName("open_ad_id")
    public String id_open_ad;
    @SerializedName("applovin_sdk_key")
    public String applovin_sdk_key;
    @SerializedName("is_noti")
    public boolean is_noti;


    // new version server
    @SerializedName("min_build")
    public int min_build;
    @SerializedName("id_banner_med")
    public String id_banner_med;
    @SerializedName("id_banner_med_home")
    public String id_banner_med_home;
    @SerializedName("id_full_med")
    public String id_full_med;
    @SerializedName("id_reward_med")
    public String id_reward_med;
    @SerializedName("id_open_med")
    public String id_open_med;
    @SerializedName("daily_percent_admob")
    public int daily_percent_admob;

//    @SerializedName("daily_percent_admob")
//    public int daily_percent_admob;
//    @SerializedName("is_ads")
//    public boolean is_ads;

}
