package com.muicvtools.mutils.downloads;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.muicvtools.mutils.Utils;
import com.muicvtools.mutils.downloads.twitter.TwitterMedia;
import com.muicvtools.mutils.downloads.twitter.TwitterPost;
import com.muicvtools.mutils.downloads.twitter.TwitterVariants;
import com.muicvtools.mutils.downloads.twitter.VmapUrl;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TwitterVideoFetch {
    private static final String AUTHORIZATION_HEADER = "Bearer AAAAAAAAAAAAAAAAAAAAAIK1zgAAAAAA2tUWuhGZ2JceoId5GwYWU5GspY4%3DUq7gzFoCZs1QfwGoVdvSac3IniczZEYXIcDyumCauIXpcAPorE";
    //demo url https://twitter.com/Britt_Ghiroli/status/1446306238078365751

    private static final String GET_TOKEN_URL = "https://api.twitter.com/1.1/guest/activate.json";
    private static final String GET_RESULT_URL = "https://api.twitter.com/1.1/statuses/show.json?id=";


    public static void getVideo(String url_source, FetchListener listener) {
        String id_video = getVideoId(url_source);
        if (id_video == null) {
            if (listener != null)
                listener.onFetchedFail(null);
            return;
        }

        getToken(url_source, id_video, listener);

    }

    private static void getToken(String urlSource, String id_video, FetchListener listener) {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        RequestBody body = RequestBody.create(new byte[]{}, null);
        Request request = new Request.Builder()
                .url(GET_TOKEN_URL)
                .addHeader("Authorization", AUTHORIZATION_HEADER)
                .post(body)
                .build();
//        try {
//            Response response = client.newCall(request).execute();
//
//            // Do something with the response.
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

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
                    TokenResult tokenResult = null;
                    try {
                        tokenResult = gson.fromJson(response.body().string(), TokenResult.class);
                    } catch (Exception e) {
                        if (listener != null)
                            listener.onFetchedFail(null);
                    }
                    if (tokenResult != null && tokenResult.getGuest_token() != null && tokenResult.getGuest_token() != "") {
                        getResult(urlSource, id_video, tokenResult.getGuest_token(), listener);
                        Utils.logDebug(this.getClass(), "Token twitter =" + tokenResult.getGuest_token());
                    }

                } else if (listener != null)
                    listener.onFetchedFail(null);

                response.close();
            }
        });
    }

    private static void getResult(String urlSource, String id_video, String token, FetchListener listener) {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        Request request = new Request.Builder()
                .url(GET_RESULT_URL + id_video + "&tweet_mode=extended")
                .addHeader("Authorization", AUTHORIZATION_HEADER)
                .addHeader("x-guest-token", token)
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
                if (listener != null) {
                    if (response.isSuccessful() && response.code() == 200) {
                        Gson gson = new GsonBuilder().create();
                        TwitterPost twitterPost = null;
                        try {
                            twitterPost = gson.fromJson(response.body().string(), TwitterPost.class);
//                            Utils.logDebug(this.getClass(), twitterPost.getTitle());
//                            Utils.logDebug(this.getClass(), twitterPost.getExtended_entities().getMedia()[0].getThumb());
                        } catch (Exception e) {
                            if (listener != null)
                                listener.onFetchedFail(null);
                            e.printStackTrace();
                            return;
                        }

                        String title = "";
                        String thumb = "";

                        if (twitterPost != null && twitterPost.getExtended_entities() != null && twitterPost.getExtended_entities().getMedia() != null && twitterPost.getExtended_entities().getMedia().length > 0) {
                            ArrayList<VideoDetail> listVideos = new ArrayList<VideoDetail>();
                            title = twitterPost.getTitle();
                            for (TwitterMedia twitterMedia : twitterPost.getExtended_entities().getMedia()) {
                                thumb = twitterMedia.getThumb();
                                if ("video".equals(twitterMedia.getType()) && twitterMedia.getVideo_info() != null && twitterMedia.getVideo_info().getVariants() != null && twitterMedia.getVideo_info().getVariants().length > 0) {
                                    for (TwitterVariants twitterInfo : twitterMedia.getVideo_info().getVariants()) {
                                        if (twitterInfo.getBitrate() > 0) {
                                            StreamQuality quality = getVideoQuality(twitterInfo.getBitrate());
                                            VideoDetail videoDetail = new VideoDetail(twitterInfo.getUrl(), quality, thumb);
                                            listVideos.add(videoDetail);
                                        }
                                    }
                                }
                            }
                            if (listVideos.size() > 0) {
                                StreamOtherInfo streamOtherInfo = new StreamOtherInfo(urlSource, DownloadType.TWITTER, listVideos, reFileName(title), thumb);
                                if (listener != null)
                                    listener.onFetchedSuccess(streamOtherInfo);
                            } else if (listener != null)
                                listener.onFetchedFail(null);

                        } else if (twitterPost != null && twitterPost.getEntities() != null && twitterPost.getEntities().getUrls() != null && twitterPost.getEntities().getUrls().length > 0) {
                            String vmapUrl = null;
                            for (VmapUrl vmap : twitterPost.getEntities().getUrls()) {
                                if (vmap.getExpanded_url() != null && vmap.getExpanded_url().contains("")) {
                                    vmapUrl = vmap.getExpanded_url();
                                    break;
                                }
                            }
                            if (vmapUrl != null) {
                                title = twitterPost.getTitle();
                                if (twitterPost.getUser() != null)
                                    thumb = twitterPost.getUser().getProfile_image_url_https();

                                getVmapUrl(urlSource, vmapUrl, title, thumb, listener);
                            } else listener.onFetchedFail(null);
                        } else
                            listener.onFetchedFail(null);
                    } else if (listener != null)
                        listener.onFetchedFail(null);
                }
                response.close();
            }
        });
    }

    private static void getVmapUrl(String urlSource, String url_twing, String title, String thumb, FetchListener listener) {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        Request request = new Request.Builder()
                .url(url_twing)
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
                if (listener != null) {
                    if (response.isSuccessful() && response.code() == 200) {
                        String result = response.body().string();

                        String vmap_url = null;
                        int amplify = result.indexOf("twitter:amplify:vmap");
                        if (amplify > 0) {
                            final String VMAP = "content=\"";
                            int startVmap = result.indexOf(VMAP, amplify);
                            if (startVmap > 0) {
                                int endVmap = result.indexOf("\"", startVmap + VMAP.length());
                                if (endVmap > 0) {
                                    vmap_url = result.substring(startVmap + VMAP.length(), endVmap);
                                    Utils.logDebug(this.getClass(), "Vmap url = " + vmap_url);
                                }
                            }
                        }

                        if (vmap_url != null)
                            getVideoFromVmap(urlSource, vmap_url, title, thumb, listener);
                        else
                            listener.onFetchedFail(null);

                    } else {
                        listener.onFetchedFail(null);
                    }
                }
                response.close();
            }
        });
    }

    private static void getVideoFromVmap(String urlSource, String url_vmap, String title, String thumb, FetchListener listener) {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        Request request = new Request.Builder()
                .url(url_vmap)
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
                if (listener != null) {
                    if (response.isSuccessful() && response.code() == 200) {
                        String result = response.body().string();
                        final String CHAR_START_VIDEO_LIST = "<tw:videoVariants>";
                        final String CHAR_END_VIDEO_LIST = "</tw:videoVariants>";

                        ArrayList<VideoDetail> listVideos = new ArrayList<VideoDetail>();
                        int start_video_list = result.indexOf(CHAR_START_VIDEO_LIST);
                        if (start_video_list > 0) {
                            int end_video_list = result.indexOf(CHAR_END_VIDEO_LIST, start_video_list);
                            if (end_video_list > 0) {
                                String str_videos = result.substring(start_video_list + CHAR_START_VIDEO_LIST.length() + 1, end_video_list);
                                String[] arr_videos = str_videos.split("\n");
                                if (arr_videos.length > 0)
                                    for (String video : arr_videos) {
                                        if(!video.contains("content_type=\"video/mp4\""))
                                            break;

                                        int start_url = video.indexOf("url=\"");
                                        if(start_url > 0)
                                        {
                                            int end_url = video.indexOf("\"",start_url + "url=\"".length());
                                            if(end_url > 0)
                                            {
                                                String unEscapeUrl = video.substring(start_url + "url=\"".length(),end_url);
                                                String url_stream = URLDecoder.decode(unEscapeUrl, "UTF-8");;//StringEscapeUtils.unescapeJava(unEscapeUrl);
                                                Utils.logDebug(this.getClass(),url_stream);

                                                int start_bitrate = video.indexOf("bit_rate=\"");
                                                if(start_bitrate >0)
                                                {
                                                    int end_bitrate = video.indexOf("\"",start_bitrate + "bit_rate=\"".length());
                                                    String str_bitrate = video.substring(start_bitrate + "bit_rate=\"".length(),end_bitrate);
                                                    try{
                                                        long int_bitrate = Long.valueOf(str_bitrate);
                                                        Utils.logDebug(this.getClass(),"quality "+getVideoQuality(int_bitrate));
                                                        listVideos.add(new VideoDetail(url_stream,getVideoQuality(int_bitrate),thumb));
                                                    }
                                                    catch (NumberFormatException e)
                                                    {

                                                    }
                                                }
                                            }
                                        }
                                    }
                            }
                        }

                        if(listVideos.size() > 0)
                        {
                            listener.onFetchedSuccess(new StreamOtherInfo(urlSource,DownloadType.TWITTER,listVideos,reFileName(title),thumb));
                        }
                        else
                            listener.onFetchedFail(null);

                    } else {
                        listener.onFetchedFail(null);
                    }
                }
                response.close();
            }
        });
    }

    private static String getVideoId(String url_source) {
        if (url_source.contains("twitter.com") && url_source.contains("/status/")) {
            if (url_source.contains("?")) {
                String base_url = url_source.split("\\?")[0];
                String[] split_id = url_source.split("/");
                if (split_id.length >= 5)
                    return split_id[5];
            } else {
                String[] split_id = url_source.split("/");
                if (split_id.length >= 5)
                    return split_id[5];
            }
        }
        return null;
    }

    private static StreamQuality getVideoQuality(long bitrate) {
        if (bitrate >= 6000000)
            return StreamQuality.HD_1080P;
        if (bitrate >= 2000000)
            return StreamQuality.HD_720P;
        if (bitrate >= 760000)
            return StreamQuality.SD_480P;
        if (bitrate >= 250000)
            return StreamQuality.SD_360P;
        return StreamQuality.SD_240P;
    }

    private static String reFileName(String name) {
        if (name == null)
            return "Twitter video";
        if (name.length() > 101)
            return name.substring(0, 100);
        else
            return name;
    }
}
