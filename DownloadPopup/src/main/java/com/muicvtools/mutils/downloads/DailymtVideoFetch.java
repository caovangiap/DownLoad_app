package com.muicvtools.mutils.downloads;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.muicvtools.mutils.Utils;
import com.muicvtools.mutils.downloads.dailymt.DailymtAuto;
import com.muicvtools.mutils.downloads.dailymt.DailymtInfo;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DailymtVideoFetch {
    private static final String GET_VIDEO_ID_URL = "https://www.dailymotion.com/player/metadata/video/%s";
    private static final String STRING_NAME = "NAME=\"";
    private static final String STRING_URI = "PROGRESSIVE-URI=\"";
    private static final String END_SEARCH = "\"";

    public static void getVideo(String url_source, FetchListener listener) {
        String idVideo = getVideoId(url_source);
        if (idVideo != null) {
            getVideoInfo(url_source, idVideo, listener);
        }
    }

    private static void getVideoInfo(String url_source, String idVideo, FetchListener listener) {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        Request request = new Request.Builder()
                .url(String.format(GET_VIDEO_ID_URL, idVideo))
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
                    DailymtInfo dailymtInfo = null;
                    try {
                        dailymtInfo = gson.fromJson(response.body().string(), DailymtInfo.class);
                    } catch (Exception e) {
                        if (listener != null)
                            listener.onFetchedFail(null);

                        response.close();
                        return;
                    }
                    if (dailymtInfo != null && dailymtInfo.getQualities() != null && dailymtInfo.getQualities().getAuto() != null && dailymtInfo.getQualities().getAuto().length > 0) {
                        String url_m3u8 = null;
                        for (DailymtAuto quality : dailymtInfo.getQualities().getAuto()) {
                            if ("application/x-mpegURL".equals(quality.getType()) )
                                url_m3u8 = quality.getUrl();
                        }
                        if (url_m3u8 != null)
                            getResult(url_source, url_m3u8,dailymtInfo, listener);
                        else if(listener != null)
                            listener.onFetchedFail(null);

                        Utils.logDebug(this.getClass(), "Video m3u8 dailymotion =" + url_m3u8);
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

    private static void getResult(String url_source, String url_m3u8,DailymtInfo dailymtInfo, FetchListener listener) {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        Request request = new Request.Builder()
                .url(url_m3u8)
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
                    String result = response.body().string();

                    ArrayList<VideoDetail> listVideos = new ArrayList<VideoDetail>();
                    String [] array_hls = result.split("\n");
                    for(String hls: array_hls)
                    {
                        if(hls.contains(STRING_NAME) && hls.contains("PROGRESSIVE-URI"))
                        {
                            int start_name = hls.indexOf(STRING_NAME);
                            if(start_name > 0)
                            {
                                int end_name = hls.indexOf(END_SEARCH,start_name + STRING_NAME.length());
                                if(end_name > 0)
                                {
                                    String quality = hls.substring(start_name + STRING_NAME.length(),end_name);

                                    StreamQuality streamQuality = getVideoQuality(quality);
                                    boolean isExitsQuality = false;
                                    for(VideoDetail video: listVideos)
                                    {
                                        if(streamQuality == video.getQuality() ) {
                                            isExitsQuality = true;
                                            break;
                                        }
                                    }
                                    if(isExitsQuality)
                                        continue;

                                    int start_stream = hls.indexOf(STRING_URI);
                                    if(start_stream > 0)
                                    {
                                        int end_stream = hls.indexOf(END_SEARCH,start_stream + STRING_URI.length());
                                        if(end_stream > 0)
                                        {
                                            String url_stream = hls.substring(start_stream + STRING_URI.length(),end_stream);
                                            Utils.logDebug(this.getClass(),"Quality = "+quality +" - url ="+url_stream);
                                            VideoDetail videoDetail = new VideoDetail(url_stream,streamQuality,getThumb(dailymtInfo));
                                            listVideos.add(videoDetail);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if(listVideos.size() > 0)
                    {
                        System.out.println("title: " + dailymtInfo.getTitle());
                        StreamOtherInfo  streamOtherInfo = new StreamOtherInfo(url_source,DownloadType.DAILYMOTION,listVideos,dailymtInfo.getTitle(),getThumb(dailymtInfo));
                        if(listener != null)
                            listener.onFetchedSuccess(streamOtherInfo);
                    }
                    else if(listener != null)
                        listener.onFetchedFail(null);
                }
                else if(listener != null)
                    listener.onFetchedFail(null);

                response.close();
            }
        });

    }

    private static String getThumb(DailymtInfo dailymtInfo)
    {
        if(dailymtInfo.getPosters() != null)
        {
            if(dailymtInfo.getPosters().containsKey("180"))
                return dailymtInfo.getPosters().get("180");
            if(dailymtInfo.getPosters().containsKey("240"))
                return dailymtInfo.getPosters().get("240");
            if(dailymtInfo.getPosters().containsKey("120"))
                return dailymtInfo.getPosters().get("120");
            if(dailymtInfo.getPosters().containsKey("360"))
                return dailymtInfo.getPosters().get("360");
            if(dailymtInfo.getPosters().containsKey("480"))
                return dailymtInfo.getPosters().get("480");
            if(dailymtInfo.getPosters().containsKey("60"))
                return dailymtInfo.getPosters().get("60");
            if(dailymtInfo.getPosters().containsKey("720"))
                return dailymtInfo.getPosters().get("720");
            if(dailymtInfo.getPosters().containsKey("1080"))
                return dailymtInfo.getPosters().get("1080");
        }

        return null;
    }

    private static StreamQuality getVideoQuality(String quality) {
        String lowercase_quality = quality.toLowerCase();
        if(lowercase_quality.startsWith("240"))
            return StreamQuality.SD_240P;
        else if(lowercase_quality.startsWith("380"))
            return StreamQuality.SD_360P;
        else if(lowercase_quality.startsWith("360"))
            return StreamQuality.SD_360P;
        else if(lowercase_quality.startsWith("480"))
            return StreamQuality.SD_480P;
        else if(lowercase_quality.startsWith("640"))
            return StreamQuality.SD_640P;
        else if(lowercase_quality.startsWith("720"))
            return StreamQuality.HD_720P;
        else if(lowercase_quality.startsWith("1080"))
            return StreamQuality.HD_1080P;
        else if(lowercase_quality.startsWith("1440"))
            return StreamQuality.HD_2K;
        else if(lowercase_quality.startsWith("2160"))
            return StreamQuality.HD_4K;
        else if(lowercase_quality.startsWith("4320"))
            return StreamQuality.HD_8K;
        else if(lowercase_quality.startsWith("144"))
            return StreamQuality.SD_180P;

        else
            return StreamQuality.SD;
    }

    private static String getVideoId(String url_source) {
        if (url_source.contains("/video/")) {
            String ids = url_source.split("/video/")[1];
            if (ids.contains("?"))
                return ids.split("\\?")[0];
            else
                return ids;

        } else if (url_source.contains("dai.ly/")) {
            String ids = url_source.split("dai.ly/")[1];
            if (ids.contains("?"))
                return ids.split("\\?")[0];
            else
                return ids;
        }
        else
            return null;
    }
}
