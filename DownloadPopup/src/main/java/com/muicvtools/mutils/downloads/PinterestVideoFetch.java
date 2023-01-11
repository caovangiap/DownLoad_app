package com.muicvtools.mutils.downloads;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.muicvtools.mutils.Utils;
import com.muicvtools.mutils.downloads.pinterest.PiPin;
import com.muicvtools.mutils.downloads.pinterest.PiVideo;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PinterestVideoFetch {
    private static final String GET_RESULT_URL = "https://www.pinterest.com/resource/PinResource/get/";
    private static final String PARAMETER = "{\"options\":{\"field_set_key\": \"unauth_react_main_pin\",\"id\": \"%s\"}}";

    public static void getVideo(String url_source, FetchListener listener) {
        if (url_source.contains("pinterest.com/pin/")) {
            String[] url_split_1 = url_source.split("pinterest.com/pin/");
            String idVideo = null;
            if (url_split_1[1].contains("/")) {
                idVideo = url_split_1[1].split("/")[0];
            } else if (url_split_1[1].contains("?")) {
                idVideo = url_split_1[1].split("\\?")[0];
            } else
                idVideo = url_split_1[1];

            Log.d("adsdk", "id_video = " + idVideo);
            getResult(url_source, idVideo, listener);
        } else
            getVideoId(url_source, listener);
    }

    private static void getVideoId(String urlSource, FetchListener listener) {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        Request request = new Request.Builder()
                .url(urlSource)
                .build();

        builder.build().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
                if (listener != null)
                    listener.onFetchedFail(null);
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.code() == 200) {
                    if (response.request().url().toString() != null && response.request().url().toString().contains("pinterest.com/pin/")) {
                        getVideo(response.request().url().toString(), listener);
                    } else if (listener != null)
                        listener.onFetchedFail(null);
                } else if (listener != null)
                    listener.onFetchedFail(null);

                response.close();
            }
        });
    }

    private static void getResult(String urlSource, String idVideo, FetchListener listener) {
//        String parameter = URLEncoder.encode(String.format(PARAMETER,idVideo), "utf-8") ;

        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        Request request = new Request.Builder()
                .url(GET_RESULT_URL + "?data=" + String.format(PARAMETER, idVideo))
                .build();

        builder.build().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
                if (listener != null)
                    listener.onFetchedFail(null);
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                Utils.logDebug(this.getClass(), "response = " + response.code());
                if (response.isSuccessful() && response.code() == 200) {
                    Gson gson = new GsonBuilder().create();
                    String result = response.body().string();

                    System.out.println("result: " + result);
                    PiPin piPin = null;
                    try {
                        piPin = gson.fromJson(result, PiPin.class);
                    } catch (Exception e) {
                        System.out.println("error: " + e);
                        if (listener != null)
                            listener.onFetchedFail(null);

                        response.close();
                        return;
                    }

                    if (listener != null) {
                        if(piPin.getResource_response().getData().getVideos() != null){
                            if (piPin != null && piPin.getResource_response() != null && piPin.getResource_response().getData() != null && piPin.getResource_response().getData().getVideos() != null) {
                                String title = null;
                                if (piPin.getResource_response().getData().getTitle() != null)
                                    title = piPin.getResource_response().getData().getTitle();
                                else if (piPin.getResource_response().getData().getGrid_title() != null)
                                    title = piPin.getResource_response().getData().getGrid_title();

                                Utils.logDebug(this.getClass(), "Title = " + title);

                                for (String keyQuality : piPin.getResource_response().getData().getVideos().getVideo_list().keySet()) {
                                    if ("V_720P".equals(keyQuality)) {
                                        PiVideo piVideo = piPin.getResource_response().getData().getVideos().getVideo_list().get(keyQuality);
                                        Utils.logDebug(this.getClass(), "url = " + piVideo.getUrl());
                                        Utils.logDebug(this.getClass(), "thumb = " + piVideo.getThumbnail());

                                        ArrayList<VideoDetail> listVideos = new ArrayList<VideoDetail>();
                                        VideoDetail videoDetail = new VideoDetail(piVideo.getUrl(), StreamQuality.HD, piVideo.getThumbnail());
                                        listVideos.add(videoDetail);

                                        StreamOtherInfo streamOtherInfo = new StreamOtherInfo(urlSource, DownloadType.VIMEO, listVideos, reFileName(title), piVideo.getThumbnail());
                                        listener.onFetchedSuccess(streamOtherInfo);
                                        return;
                                    }

                                    Utils.logDebug(this.getClass(), "key = " + keyQuality);
                                }
                            }
                        } else if (piPin.getResource_response().getData().getStory_pin_data() != null) {
                            if (piPin != null && piPin.getResource_response() != null && piPin.getResource_response().getData() != null && piPin.getResource_response().getData().getStory_pin_data() != null) {
                                String title = null;
                                if (piPin.getResource_response().getData().getTitle() != null)
                                    title = piPin.getResource_response().getData().getTitle();
                                else if (piPin.getResource_response().getData().getGrid_title() != null)
                                    title = piPin.getResource_response().getData().getGrid_title();

                                Utils.logDebug(this.getClass(), "Title = " + title);

                                ArrayList<VideoDetail> listVideos = new ArrayList<VideoDetail>();
                                PiVideo piVideo = null;

                                for (String keyQuality : piPin.getResource_response().getData().getStory_pin_data().getPages().get(0).getBlocks().get(0).getVideo().getVideo_list().keySet()) {
                                    if ("V_EXP7".equals(keyQuality)) {
                                        piVideo = piPin.getResource_response().getData().getStory_pin_data().getPages().get(0).getBlocks().get(0).getVideo().getVideo_list().get(keyQuality);
                                        Utils.logDebug(this.getClass(), "url = " + piVideo.getUrl());
                                        Utils.logDebug(this.getClass(), "thumb = " + piVideo.getThumbnail());

                                        VideoDetail videoDetail = new VideoDetail(piVideo.getUrl(), StreamQuality.HD, piVideo.getThumbnail());
                                        listVideos.add(videoDetail);
                                    }
                                    if ("V_EXP5".equals(keyQuality)){
                                        piVideo = piPin.getResource_response().getData().getStory_pin_data().getPages().get(0).getBlocks().get(0).getVideo().getVideo_list().get(keyQuality);
                                        Utils.logDebug(this.getClass(), "url = " + piVideo.getUrl());
                                        Utils.logDebug(this.getClass(), "thumb = " + piVideo.getThumbnail());

                                        VideoDetail videoDetail = new VideoDetail(piVideo.getUrl(), StreamQuality.SD, piVideo.getThumbnail());
                                        listVideos.add(videoDetail);
                                    }

                                    Utils.logDebug(this.getClass(), "key = " + keyQuality);
                                }
                                StreamOtherInfo streamOtherInfo = new StreamOtherInfo(urlSource, DownloadType.VIMEO, listVideos, reFileName(title), piVideo.getThumbnail());
                                listener.onFetchedSuccess(streamOtherInfo);
                            }
                        }

                        listener.onFetchedFail(null);
                    }
                }

                response.close();
            }
        });
    }

    private static String reFileName(String name) {
        if (name == null || "".equals(name))
            return "Pinterest video";
        if (name.length() > 101)
            return name.substring(0, 100);
        else
            return name;
    }
}
