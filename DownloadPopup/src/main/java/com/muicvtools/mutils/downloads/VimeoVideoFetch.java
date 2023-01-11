package com.muicvtools.mutils.downloads;

import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.muicvtools.mutils.Utils;
import com.muicvtools.mutils.downloads.vimeo.VimeoConfig;
import com.muicvtools.mutils.downloads.vimeo.VimeoInfo;
import com.muicvtools.mutils.downloads.vimeo.VimeoVideo;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VimeoVideoFetch {
    private static final String GET_VIDEO_ID_URL = "https://vimeo.com/api/oembed.json?url=%s";
    private static final String GET_RESULT_URL = "https://player.vimeo.com/video/%s/config";

    public static void getVideo(String url_source, FetchListener listener) {

        getVideoId(url_source, listener);
    }

    private static void getVideoId(String urlSource, FetchListener listener) {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        Request request = new Request.Builder()
                .url(String.format(GET_VIDEO_ID_URL, Uri.encode(urlSource)))
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
                    Gson gson = new GsonBuilder().create();
                    VimeoInfo vimeoInfo = null;
                    try {
                        vimeoInfo = gson.fromJson(response.body().string(), VimeoInfo.class);
                    } catch (Exception e) {
                        if (listener != null)
                            listener.onFetchedFail(null);

                        response.close();
                        return;
                    }
                    if (vimeoInfo != null && vimeoInfo.getTitle() != null) {
                        getResult(urlSource, vimeoInfo, listener);
                        Utils.logDebug(this.getClass(), "Video id vimeo =" + vimeoInfo.getVideo_id());
                    } else {
                        if (listener != null)
                            listener.onFetchedFail("Cannot download private or password videos");
                    }

                } else if (listener != null)
                    listener.onFetchedFail(null);

                response.close();
            }
        });
    }

    private static void getResult(String urlSource, VimeoInfo vimeoInfo, FetchListener listener) {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        Request request = new Request.Builder()
                .url(String.format(GET_RESULT_URL, vimeoInfo.getVideo_id()))
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
                    VimeoConfig vimeoConfig = null;
                    try {
                        vimeoConfig = gson.fromJson(response.body().string(), VimeoConfig.class);
                    } catch (Exception e) {
                        if (listener != null)
                            listener.onFetchedFail(null);

                        response.close();
                        return;
                    }
                    ArrayList<VideoDetail> listVideos = new ArrayList<VideoDetail>();
                    if (vimeoConfig != null && vimeoConfig.getRequest() != null && vimeoConfig.getRequest().getFiles() != null && vimeoConfig.getRequest().getFiles().getProgressive() != null
                            && vimeoConfig.getRequest().getFiles().getProgressive().length > 0) {
                        for (VimeoVideo video : vimeoConfig.getRequest().getFiles().getProgressive()) {
                            VideoDetail videoDetail = new VideoDetail(video.getUrl(),getVideoQuality(video.getQuality()),vimeoInfo.getThumbnail_url());
                            listVideos.add(videoDetail);
                        }
                    }

                    if(listener != null)
                    {
                        if(listVideos.size() > 0)
                        {
                            StreamOtherInfo streamOtherInfo = new StreamOtherInfo(urlSource,DownloadType.VIMEO,listVideos,vimeoInfo.getTitle(),vimeoInfo.getThumbnail_url());
                            listener.onFetchedSuccess(streamOtherInfo);
                        }
                        else
                            listener.onFetchedFail(null);
                    }
                }

                response.close();
            }
        });
    }

    private static StreamQuality getVideoQuality(String quality) {
        String lowercase_quality = quality.toLowerCase();

        switch (quality) {
            case "8k":
                return StreamQuality.HD_8K;
            case "4k":
                return StreamQuality.HD_4K;
            case "2k":
                return StreamQuality.HD_2K;
            case "1080p":
                return StreamQuality.HD_1080P;
            case "720p":
                return StreamQuality.HD_720P;
            case "640p":
                return StreamQuality.SD_640P;
            case "540p":
                return StreamQuality.SD_540P;
            case "480p":
                return StreamQuality.SD_480P;
            case "360p":
                return StreamQuality.SD_360P;
            case "240p":
                return StreamQuality.SD_240P;
            case "180p":
                return StreamQuality.SD_180P;

            default:
                return StreamQuality.UNKOW;
        }
    }


}
