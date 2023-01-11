package com.muicvtools.mutils.downloads;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.muicvtools.mutils.Utils;

import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FbVideoFetch {
    private static FacebookType currentType = FacebookType.NONE;

    private static final String START_VIDEO_OBJECT = "\"\\u0040type\":\"VideoObject\"";
    private static final String END_VIDEO_OBJECT = "\"\\u0040context\":\"https:\\/\\/schema.org\"";

    private static final String START_PLAY_HD = "\"playable_url_quality_hd\":\"";
    private static final String END_PLAY_HD = "\"";

    private static final String START_PLAY_HD_2 = "hd_src:\"";
    private static final String END_PLAY_HD_2 = "\"";

    private static final String START_PLAY_SD = "\"playable_url\":\"";
    private static final String END_PLAY_SD = "\"";

    private static final String START_PLAY_SD_2 = "sd_src:\"";
    private static final String END_PLAY_SD_2 = "\"";

    private static final String START_TITLE = "\"story\":{\"message\":{\"text\":\"";
    private static final String START_TITLE_1 = ",\"message\":{\"text\":\"";
    private static final String START_TITLE_2 = "],\"text\":\"";
    private static final String END_TITLE = "\"";

    private static final String START_THUMB = "\"preferred_thumbnail\":{\"image\":{\"uri\":\"";
    //    private static final String START_THUMB = "\"image\":{\"uri\":\"";
    private static final String END_THUMB = "\"}";

    public static void getVideo(String url_source, FetchListener listener) {
        if (url_source.contains("fb.watch") || url_source.contains("fb.gg") || url_source.contains("facebook.com/watch")) {
            currentType = FacebookType.WATCH;
        }

        String urlRequest = url_source.replace("m.facebook.com", "facebook.com");

        OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
                .followRedirects(true);
        if (FacebookType.WATCH != currentType)
            builder.cookieJar(new WebviewCookieHandler());

        Request request = new Request.Builder()
                .url(urlRequest.trim())
                .addHeader("Accept", DownloadConstants.ACCEPT)
                .addHeader("Sec-Fetch-Mode", "navigate")
                .addHeader("User-agent", DownloadConstants.USER_AGENT_FIREFOX)
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
                Utils.logDebug(this.getClass(), "Response url= " + response.request().url());
                Utils.logDebug(this.getClass(), "Response code= " + response.code());
                if (response.request().url() != null && response.request().url().toString().contains("facebook.com/watch") && FacebookType.WATCH != currentType) {
                    getVideo(response.request().url().toString(), listener);
                    return;
                }

                if (response.isSuccessful()) {
                    String result = response.body().string();
                    if (FacebookType.WATCH == currentType) {
                        String jsonString = null;
                        FbVideo fbVideo = null;

                        int startVideo = result.indexOf(START_VIDEO_OBJECT);
                        if (startVideo > 0) {
                            int endVideo = result.indexOf(END_VIDEO_OBJECT, startVideo);
                            if (endVideo > 0) {
                                jsonString = result.substring(startVideo - 1, endVideo + END_VIDEO_OBJECT.length() + 1);

                                Gson gson = new GsonBuilder().create();
                                try {
                                    fbVideo = gson.fromJson(jsonString, FbVideo.class);
                                    if (fbVideo.name != null)
                                        fbVideo.name = StringEscapeUtils.unescapeJava(fbVideo.name);
                                    if (fbVideo.sd_Url != null)
                                        fbVideo.sd_Url = StringEscapeUtils.unescapeJava(fbVideo.sd_Url);
                                    if (fbVideo.thumbnailUrl != null)
                                        fbVideo.thumbnailUrl = StringEscapeUtils.unescapeJava(fbVideo.thumbnailUrl);

                                } catch (JsonSyntaxException e) {
                                }
                            }
                        }
                        if (fbVideo == null) {
                            fbVideo = new FbVideo();
                            fbVideo.name = "Facebook video";
                            fbVideo.thumbnailUrl = null;

                            String url_thumb = null;
                            int startThumb = result.indexOf(START_THUMB);
                            if (startThumb > 0) {
                                int endThumb = result.indexOf(END_THUMB, startThumb + START_THUMB.length());
                                if (endThumb > 0) {
                                    String unEs_Url_thumb = result.substring(startThumb + START_THUMB.length(), endThumb);
                                    url_thumb = StringEscapeUtils.unescapeJava(unEs_Url_thumb);
                                    fbVideo.thumbnailUrl = url_thumb;
                                    Utils.logDebug(this.getClass(), "Thumb = " + url_thumb);
                                }
                            } /*else {
                                startThumb = result.indexOf(START_THUMB_1);
                                if (startThumb > 0) {
                                    int endThumb = result.indexOf(END_THUMB, startThumb + START_THUMB_1.length());
                                    if (endThumb > 0) {
                                        String unEs_Url_thumb = result.substring(startThumb + START_THUMB_1.length(), endThumb);
                                        url_thumb = StringEscapeUtils.unescapeJava(unEs_Url_thumb);
                                        fbVideo.thumbnailUrl = url_thumb;
                                        Utils.logDebug(this.getClass(), "Thumb-1 = " + url_thumb);
                                    }
                                }
                            }*/

                            String title = null;
                            int startTitle = result.indexOf(START_TITLE);
                            if (startTitle > 0) {
                                int endTitle = result.indexOf(END_TITLE, startTitle + START_TITLE.length());
                                if (endTitle > 0) {
                                    String unEsTitle = result.substring(startTitle + START_TITLE.length(), endTitle);
                                    title = StringEscapeUtils.unescapeJava(unEsTitle).replace("\n", " ");
                                    fbVideo.name = title;
                                    Utils.logDebug(this.getClass(), "Title1 = " + title);
                                }
                            } else {
                                startTitle = result.indexOf(START_TITLE_1);
                                if(startTitle > 0){
                                    int endTitle = result.indexOf(END_TITLE, startTitle + START_TITLE_1.length());
                                    if (endTitle > 0) {
                                        String unEsTitle = result.substring(startTitle + START_TITLE_1.length(), endTitle);
                                        title = StringEscapeUtils.unescapeJava(unEsTitle).replace("\n", " ");
                                        fbVideo.name = title;
                                        Utils.logDebug(this.getClass(), "Title1 = " + title);
                                    }
                                } else {
                                    int index = 0;
                                    if(result.indexOf(START_TITLE_2, index) > 0) {
                                        index = result.indexOf(START_TITLE_2) + 1;
                                    }
                                    startTitle = result.indexOf(START_TITLE_2, index);

                                    if(startTitle > 0){
                                        int endTitle = result.indexOf(END_TITLE, startTitle + START_TITLE_2.length());
                                        if (endTitle > 0) {
                                            String unEsTitle = result.substring(startTitle + START_TITLE_2.length(), endTitle);
                                            title = StringEscapeUtils.unescapeJava(unEsTitle).replace("\n", " ");
                                            fbVideo.name = title;
                                            Utils.logDebug(this.getClass(), "Title1 = " + title);
                                        }
                                    }
                                }
                            }

                            if (fbVideo.sd_Url == null || "".equals(fbVideo.sd_Url)) {
                                int startSD = result.indexOf(START_PLAY_SD);
                                if (startSD > 0) {
                                    int endSD = result.indexOf(END_PLAY_SD, startSD + START_PLAY_SD.length());
                                    if (endSD > 0) {
                                        String unEscapeUrl = result.substring(startSD + START_PLAY_SD.length(), endSD);
                                        fbVideo.sd_Url = StringEscapeUtils.unescapeJava(unEscapeUrl);
                                    } else
                                        fbVideo.sd_Url = null;
                                }
                            }
                        }

                        String hd_url = null;
                        int startHD = result.indexOf(START_PLAY_HD);
                        if (startHD > 0) {
                            int endHD = result.indexOf(END_PLAY_HD, startHD + START_PLAY_HD.length());
                            if (endHD > 0) {
                                String unEscapeUrl = result.substring(startHD + START_PLAY_HD.length(), endHD);
                                hd_url = StringEscapeUtils.unescapeJava(unEscapeUrl);
                                Utils.logDebug(this.getClass(), "HD 1 = " + hd_url);
                            }
                        } else {
                            int startHD2 = result.indexOf(START_PLAY_HD_2);
                            if (startHD2 > 0) {
                                int endHD = result.indexOf(END_PLAY_HD_2, startHD2 + START_PLAY_HD_2.length());
                                if (endHD > 0) {
                                    String unEscapeUrl = result.substring(startHD2 + START_PLAY_HD_2.length(), endHD);
                                    hd_url = StringEscapeUtils.unescapeJava(unEscapeUrl);
                                    Utils.logDebug(this.getClass(), "HD 2 = " + hd_url);
                                }
                            }
                        }

                        ArrayList<VideoDetail> listVideo = new ArrayList<VideoDetail>();
                        if (fbVideo.sd_Url != null)
                            listVideo.add(new VideoDetail(fbVideo.sd_Url, StreamQuality.SD, fbVideo.thumbnailUrl));
                        if (hd_url != null)
                            listVideo.add(new VideoDetail(hd_url, StreamQuality.HD, fbVideo.thumbnailUrl));

                        if (listVideo.size() == 0 && listener != null)
                            listener.onFetchedFail(null);
                        else if (listener != null) {
                            fbVideo.name = reFileName(fbVideo.name);
                            StreamOtherInfo streamOtherInfo = new StreamOtherInfo(url_source, DownloadType.FACEBOOK, listVideo, fbVideo.name, fbVideo.thumbnailUrl);
                            listener.onFetchedSuccess(streamOtherInfo);
                        }

                    } else {
                        FbVideo fbVideo = null;
                        int startVideo = result.indexOf(START_VIDEO_OBJECT);
                        if (startVideo > 0) {
                            int endVideo = result.indexOf(END_VIDEO_OBJECT, startVideo);
                            if (endVideo > 0) {
                                String jsonString = result.substring(startVideo - 1, endVideo + END_VIDEO_OBJECT.length() + 1);
                                Gson gson = new GsonBuilder().create();
                                try {
                                    fbVideo = gson.fromJson(jsonString, FbVideo.class);
                                    if (fbVideo.name != null)
                                        fbVideo.name = StringEscapeUtils.unescapeJava(fbVideo.name);
                                    if (fbVideo.sd_Url != null)
                                        fbVideo.sd_Url = StringEscapeUtils.unescapeJava(fbVideo.sd_Url);
                                    if (fbVideo.thumbnailUrl != null)
                                        fbVideo.thumbnailUrl = StringEscapeUtils.unescapeJava(fbVideo.thumbnailUrl);

                                    Utils.logDebug(this.getClass(), "VideoObject = " + fbVideo.name);
                                } catch (JsonSyntaxException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        if (fbVideo == null) {
                            fbVideo = new FbVideo();

                            String url_thumb = null;
                            int startThumb = result.indexOf(START_THUMB);
                            if (startThumb > 0) {
                                int endThumb = result.indexOf(END_THUMB, startThumb + START_THUMB.length());
                                if (endThumb > 0) {
                                    String unEs_Url_thumb = result.substring(startThumb + START_THUMB.length(), endThumb);
                                    url_thumb = StringEscapeUtils.unescapeJava(unEs_Url_thumb);
                                    fbVideo.thumbnailUrl = url_thumb;
                                    Utils.logDebug(this.getClass(), "Thumb = " + url_thumb);
                                }
                            } /*else {
                                startThumb = result.indexOf(START_THUMB_1);
                                if (startThumb > 0) {
                                    int endThumb = result.indexOf(END_THUMB, startThumb + START_THUMB_1.length());
                                    if (endThumb > 0) {
                                        String unEs_Url_thumb = result.substring(startThumb + START_THUMB_1.length(), endThumb);
                                        url_thumb = StringEscapeUtils.unescapeJava(unEs_Url_thumb);
                                        fbVideo.thumbnailUrl = url_thumb;
                                        Utils.logDebug(this.getClass(), "Thumb 1 = " + url_thumb);
                                    }
                                }
                            }*/

                            String title = null;
                            int startTitle = result.indexOf(START_TITLE);
                            if (startTitle > 0) {
                                int endTitle = result.indexOf(END_TITLE, startTitle + START_TITLE.length());
                                if (endTitle > 0) {
                                    String unEsTitle = result.substring(startTitle + START_TITLE.length(), endTitle);
                                    title = StringEscapeUtils.unescapeJava(unEsTitle).replace("\n", " ");
                                    fbVideo.name = title;
                                    Utils.logDebug(this.getClass(), "Title = " + title);
                                }
                            } else {
                                startTitle = result.indexOf(START_TITLE_1);
                                if(startTitle > 0){
                                    int endTitle = result.indexOf(END_TITLE, startTitle + START_TITLE_1.length());
                                    if (endTitle > 0) {
                                        String unEsTitle = result.substring(startTitle + START_TITLE_1.length(), endTitle);
                                        title = StringEscapeUtils.unescapeJava(unEsTitle).replace("\n", " ");
                                        fbVideo.name = title;
                                        Utils.logDebug(this.getClass(), "Title = " + title);
                                    }
                                } else {
                                    int index = 0;
                                    if(result.indexOf(START_TITLE_2, index) > 0) {
                                        index = result.indexOf(START_TITLE_2) + 1;
                                    }
                                    startTitle = result.indexOf(START_TITLE_2, index);
                                    if(startTitle > 0){
                                        int endTitle = result.indexOf(END_TITLE, startTitle + START_TITLE_2.length());
                                        if (endTitle > 0) {
                                            String unEsTitle = result.substring(startTitle + START_TITLE_2.length(), endTitle);
                                            title = StringEscapeUtils.unescapeJava(unEsTitle).replace("\n", " ");
                                            fbVideo.name = title;
                                            Utils.logDebug(this.getClass(), "Title1 = " + title);
                                        }
                                    }
                                }
                            }
                        }

                        String hd_url = null;
                        if (fbVideo.sd_Url == null || "".equals(fbVideo.sd_Url)) {
                            int startSD = result.indexOf(START_PLAY_SD);
                            if (startSD > 0) {
                                int endSD = result.indexOf(END_PLAY_SD, startSD + START_PLAY_SD.length());
                                if (endSD > 0) {
                                    String unEscapeSD = result.substring(startSD + START_PLAY_SD.length(), endSD);
                                    fbVideo.sd_Url = StringEscapeUtils.unescapeJava(unEscapeSD);

                                    int startHD = result.indexOf(START_PLAY_HD);
                                    if (startHD > 0) {
                                        int endHD = result.indexOf(END_PLAY_HD, startHD + START_PLAY_HD.length());
                                        if (endHD > 0) {
                                            String unEscapeUrl = result.substring(startHD + START_PLAY_HD.length(), endHD);
                                            hd_url = StringEscapeUtils.unescapeJava(unEscapeUrl);
                                            Utils.logDebug(this.getClass(), "HD 1 = " + hd_url);
                                        }
                                    }
                                }
                            } else {
                                int startSD2 = result.indexOf(START_PLAY_SD_2);
                                if (startSD2 > 0) {
                                    int endSD = result.indexOf(END_PLAY_SD_2, startSD2 + START_PLAY_SD_2.length());
                                    if (endSD > 0) {
                                        String unEscapeSD = result.substring(startSD2 + START_PLAY_SD_2.length(), endSD);
                                        fbVideo.sd_Url = StringEscapeUtils.unescapeJava(unEscapeSD);

                                        int startHD = result.indexOf(START_PLAY_HD_2);
                                        if (startHD > 0) {
                                            int endHD = result.indexOf(END_PLAY_HD_2, startHD + START_PLAY_HD_2.length());
                                            if (endHD > 0) {
                                                String unEscapeUrl = result.substring(startHD + START_PLAY_HD_2.length(), endHD);
                                                hd_url = StringEscapeUtils.unescapeJava(unEscapeUrl);
                                                Utils.logDebug(this.getClass(), "HD 2 = " + hd_url);

                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            int startHD = result.indexOf(START_PLAY_HD);
                            if (startHD > 0) {
                                int endHD = result.indexOf(END_PLAY_HD, startHD + START_PLAY_HD.length());
                                if (endHD > 0) {
                                    String unEscapeUrl = result.substring(startHD + START_PLAY_HD.length(), endHD);
                                    hd_url = StringEscapeUtils.unescapeJava(unEscapeUrl);
                                    Utils.logDebug(this.getClass(), "HD 1 = " + hd_url);
                                }
                            } else {
                                int startHD2 = result.indexOf(START_PLAY_HD_2);
                                if (startHD2 > 0) {
                                    int endHD = result.indexOf(END_PLAY_HD_2, startHD2 + START_PLAY_HD_2.length());
                                    if (endHD > 0) {
                                        String unEscapeUrl = result.substring(startHD2 + START_PLAY_HD_2.length(), endHD);
                                        hd_url = StringEscapeUtils.unescapeJava(unEscapeUrl);
                                        Utils.logDebug(this.getClass(), "HD 2 = " + hd_url);
                                    }
                                }
                            }
                        }

                        ArrayList<VideoDetail> listVideo = new ArrayList<VideoDetail>();
                        if (fbVideo.sd_Url != null)
                            listVideo.add(new VideoDetail(fbVideo.sd_Url, StreamQuality.SD, fbVideo.thumbnailUrl));
                        if (hd_url != null)
                            listVideo.add(new VideoDetail(hd_url, StreamQuality.HD, fbVideo.thumbnailUrl));

                        if (listVideo.size() == 0 && listener != null) {
                            if (result.contains("form id=\"login_form\"") || result.contains("login_post_uri"))
                                listener.requireLogin();
                            else
                                listener.onFetchedFail(null);
                        } else if (listener != null) {
                            fbVideo.name = reFileName(fbVideo.name);
                            StreamOtherInfo streamOtherInfo = new StreamOtherInfo(url_source, DownloadType.FACEBOOK, listVideo, fbVideo.name, fbVideo.thumbnailUrl);
                            listener.onFetchedSuccess(streamOtherInfo);
                        }

                    }
                } else {
                    if (listener != null)
                        listener.onFetchedFail(null);
                }
                response.close();
            }
        });

    }

    private static String reFileName(String name) {
        if (name == null)
            return "Facebook video";

        String titleVideo = name.trim();
        if(titleVideo.equals("")) return "Facebook video";
        else if (titleVideo.length() > 101)
            return titleVideo.substring(0, 100);
        else
            return titleVideo;

    }
}
