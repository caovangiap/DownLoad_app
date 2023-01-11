package com.muicvtools.mutils.downloads;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.muicvtools.mutils.Utils;
import com.muicvtools.mutils.downloads.insta.ModelEdge;
import com.muicvtools.mutils.downloads.insta.ModelEdgeSidecarToChildren;
import com.muicvtools.mutils.downloads.insta.ModelResponse;
import com.muicvtools.mutils.downloads.insta.tv.InstaTV;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class InstaVideoFetch {

    public static void getVideo(String url_source, FetchListener listener) {
        String urlRequest = null;
        if (url_source.contains("facebook.com") && url_source.contains("instagram.com")) {
            urlRequest = customUrl(url_source) + "?__a=1&__d=dis";
        } else if (url_source.contains("?"))
            urlRequest = url_source + "&__a=1&__d=dis";
        else
            urlRequest = url_source + "?__a=1&__d=dis";

        String url_request = changeTypeUrl(urlRequest);

        System.out.println("url_request: " + url_request);

        OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
                .followRedirects(true);

        Request request = new Request.Builder()
                .url(url_request)
                .build();
        builder.cookieJar(new WebviewCookieHandler());

        builder.build().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
                if (listener != null)
                    listener.onFetchedFail(null);
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.request().url().toString() != null && response.request().url().toString().contains("instagram.com/accounts/login/")) {
                    if (listener != null)
                        listener.requireLogin();
                    Utils.logDebug(this.getClass(), "Require login first");
                } else if (response.isSuccessful() && response.code() == 200) {
                    String result = response.body().string();

                    Gson gson = new GsonBuilder().create();
                    InstaTV instaTV = null;
                    ModelResponse modelResponse = null;
                    try {
                        if(url_request.contains("/tv/")) instaTV = gson.fromJson(result, InstaTV.class);
                        else modelResponse = gson.fromJson(result, ModelResponse.class);
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                        if (listener != null)
                            listener.onFetchedFail(null);
                        return;
                    }

                    ArrayList<VideoDetail> listVideo = new ArrayList<VideoDetail>();
                    if(url_request.contains("/tv/")){
                        try {
                            listVideo.add(new VideoDetail(instaTV.getItem().get(0).getVideoVersions().get(0).getUrl(),
                                    StreamQuality.HD,
                                    instaTV.getItem().get(0).getImageVersion2().getCandidates().get(0).getUrl()));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            if (modelResponse.getModelGraphql().getShortcode_media().isIs_video()) {
                                listVideo.add(new VideoDetail(modelResponse.getModelGraphql().getShortcode_media().getVideo_url(), StreamQuality.HD, modelResponse.getModelGraphql().getShortcode_media().getDisplay_url()));
                            } else {
                                ModelEdgeSidecarToChildren modelEdgeSidecarToChildren = modelResponse.getModelGraphql().getShortcode_media().getEdge_sidecar_to_children();
                                if (modelEdgeSidecarToChildren != null && modelEdgeSidecarToChildren.getModelEdges().size() > 0) {
                                    List<ModelEdge> modelEdgeList = modelEdgeSidecarToChildren.getModelEdges();
                                    for (ModelEdge modelEdge : modelEdgeList) {
                                        if (modelEdge.getModelNode().isIs_video())
                                            listVideo.add(new VideoDetail(modelEdge.getModelNode().getVideo_url(), StreamQuality.HD, modelEdge.getModelNode().getDisplay_url()));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (listVideo.size() == 0 && listener != null)
                        listener.onFetchedFail(null);
                    else if (listener != null) {
                        String title_tb1 = "Instagram video";
                        StreamOtherInfo streamOtherInfo = null;
                        String reName = "";
                        if(url_request.contains("/tv/")){
                            if(instaTV.getItem().get(0).getCaption() == null) title_tb1 = "Instagram video";
                            else title_tb1 = instaTV.getItem().get(0).getCaption().text;

                            reName = reFileName(title_tb1);
                            streamOtherInfo = new StreamOtherInfo(url_source, DownloadType.INSTA, listVideo, reName, instaTV.getItem().get(0).getImageVersion2().getCandidates().get(0).getUrl());
                        } else {
                            if (modelResponse.getModelGraphql().getShortcode_media() != null && modelResponse.getModelGraphql().getShortcode_media().getEdge_media_to_caption() != null
                                    && modelResponse.getModelGraphql().getShortcode_media().getEdge_media_to_caption().getModelEdges() != null
                                    && modelResponse.getModelGraphql().getShortcode_media().getEdge_media_to_caption().getModelEdges().size() > 0
                                    && modelResponse.getModelGraphql().getShortcode_media().getEdge_media_to_caption().getModelEdges().get(0).getModelNode() != null)
                                title_tb1 = modelResponse.getModelGraphql().getShortcode_media().getEdge_media_to_caption().getModelEdges().get(0).getModelNode().getText();
                            else if (modelResponse.getModelGraphql().getShortcode_media() != null && modelResponse.getModelGraphql().getShortcode_media().getTitle() != null)
                                title_tb1 = modelResponse.getModelGraphql().getShortcode_media().getTitle();

                            reName = reFileName(title_tb1);
                            streamOtherInfo = new StreamOtherInfo(url_source, DownloadType.INSTA, listVideo, reName, modelResponse.getModelGraphql().getShortcode_media().getDisplay_url());
                        }
                        Utils.logDebug(this.getClass(), listVideo.get(0).getUrl_stream());
                        listener.onFetchedSuccess(streamOtherInfo);
                    }


                } else {
                    if (listener != null)
                        listener.onFetchedFail(null);
                }

                response.close();

            }
        });

    }

    private static String changeTypeUrl(String url_source) {
        String url = url_source;
        if (url_source.contains("/reel/")) url = url_source.replace("/reel/", "/p/");
        return url;
    }

    private static String customUrl(String url_source) {
        String url = url_source.split("www.instagram.com")[1].split("&")[0];
        String replaceUrl = url.replaceAll("%2F", "\\/");
        return "https://www.instagram.com" + replaceUrl;
    }

    private static String reFileName(String name) {
        if (name == null)
            return "Instagram video";

        String titleInsta = name.trim();
        if (titleInsta.equals("")) return "Instagram video";
        else if (titleInsta.length() > 101)
            return titleInsta.substring(0, 100);
        else
            return titleInsta;
    }
}