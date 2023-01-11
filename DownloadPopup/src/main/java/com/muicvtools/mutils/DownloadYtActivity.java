package com.muicvtools.mutils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.muicvtools.mutils.downloads.DownloadType;
import com.muicvtools.mutils.downloads.FetchListener;
import com.muicvtools.mutils.downloads.SCRIPT_TYPE;
import com.muicvtools.mutils.downloads.ScriptInfo;
import com.muicvtools.mutils.downloads.ScriptUtils;
import com.muicvtools.mutils.downloads.StreamOtherInfo;
import com.muicvtools.mutils.downloads.StreamQuality;
import com.muicvtools.mutils.downloads.TiktokResult;
import com.muicvtools.mutils.downloads.VideoDetail;

import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import us.shandian.giga.io.StoredDirectoryHelper;
import us.shandian.giga.service.DownloadManager;
import us.shandian.giga.ui.DownloadDialog;
import us.shandian.giga.util.ACCEPT_DOWNLOAD;
import us.shandian.giga.util.ExtractorHelper;
import us.shandian.giga.util.FilePickerActivityHelper;
import us.shandian.giga.util.ListHelper;

@SuppressLint("SetJavaScriptEnabled")
public abstract class DownloadYtActivity extends AdOverlayActivity {
    protected abstract DownloadType getDownloadType();

    private StreamInfo result;
    private StreamOtherInfo result_other;

    private boolean isPendingShowDirectDownload = false;
    private boolean isPendingShowPlanDownload = false;
    private boolean isPendingShowTubeChange = false;
    //    private boolean isPendingShowPlanOtherSiteDownload = false;
    private boolean isPendingShowDirectOtherSiteDownload = false;


    private ACCEPT_DOWNLOAD accept_download;
    //    private StreamQuality accept_download_other_site;
    private int current_video_other_site = 0;
    private String currentDownloadYtbUrl = null;

    private Handler handler_tikTok;
    private Runnable runnable_tikTok;
    private String current_Tiktok_Url;

    protected WebView tiktokWebview;
    private ScriptInfo currentTiktokScript;
    private Long lastTimeTiktokWebviewFinishLoad = 0l;
    private final static Long TIME_RELOADWEBVIEW = 10 * 60 * 1000l;
    private boolean isPedingDownloadTiktok = false;
    private boolean isPendingLoadWebviewTiktok = false;
    private FetchListener tiktokFetchListener = null;

    private final ActivityResultLauncher<Intent> requestDownloadPickVideoFolderLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(), this::requestDownloadPickVideoFolderResult);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getDownloadType() == DownloadType.TIKTOK || getDownloadType() == DownloadType.ALL)
            preloadTittokWebview(this);
        setDefaultDownloadFolder();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (tiktokWebview != null)
            tiktokWebview.onResume();

        if (isPendingShowDirectDownload) {
            isPendingShowDirectDownload = false;
            showDirectDialogDownload();
        } else if (isPendingShowPlanDownload) {
            isPendingShowPlanDownload = false;
            showDownloadPlanDialog();
        }
//        else if (isPendingShowPlanOtherSiteDownload) {
//            isPendingShowPlanOtherSiteDownload = false;
//            downloadOtherSite(result_other, current_video_other_site);
//        }
        else if (isPendingShowDirectOtherSiteDownload) {
            isPendingShowDirectOtherSiteDownload = false;
            showDirectDialogOtherSiteDownload();
        } else if (isPendingShowTubeChange) {
            isPendingShowTubeChange = false;
            showPopupTubeChange();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (tiktokWebview != null)
            tiktokWebview.onPause();
    }

    private boolean checkSelectedDownloadFolder() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String key = this.getString(R.string.download_path_video_key);
        final String downloadPath = prefs.getString(key, null);
        if (downloadPath == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this,androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert);
            View root = getLayoutInflater().inflate(R.layout.layout_select_folder, null);
            builder.setTitle(R.string.select_folder_title)
                    .setView(root)
                    .setCancelable(true);
            AlertDialog alertDialog = builder.create();

            Button btn_select_folder = root.findViewById(R.id.btn_select_folder);
            Button btn_default_folder = root.findViewById(R.id.btn_default_folder);
            btn_select_folder.setOnClickListener(view -> {
                alertDialog.dismiss();
                launchDirectoryPicker(requestDownloadPickVideoFolderLauncher);
            });

            btn_default_folder.setOnClickListener(view -> {
                alertDialog.dismiss();

                setDefaultDownloadFolder();

                if (currentDownloadYtbUrl != null) {
                    downloadYoutube(currentDownloadYtbUrl);
                } else if (result_other != null && result_other.getUrl_type() == DownloadType.YOUTUBE) {
                    downloadYoutube(result_other, current_video_other_site);
                } else if (result_other != null) {
                    downloadOtherSite(result_other, current_video_other_site);
                }
            });
            alertDialog.show();
            return false;
        } else
            return true;
    }

    private void setDefaultDownloadFolder() {
        final String key_audio = getString(R.string.download_path_video_key);
        final String key_video = getString(R.string.download_path_audio_key);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor spEditor = prefs.edit();
        spEditor.putString(key_video, new File(getDir(Environment.DIRECTORY_DOWNLOADS), Constants.VIDEO_AUDIO_PATH).toURI().toString());
        spEditor.putString(key_audio, new File(getDir(Environment.DIRECTORY_DOWNLOADS), Constants.VIDEO_AUDIO_PATH).toURI().toString());
        spEditor.apply();
    }

    private static File getDir(final String defaultDirectoryName) {
        return new File(Environment.getExternalStorageDirectory(), defaultDirectoryName);
    }

    private void launchDirectoryPicker(final ActivityResultLauncher<Intent> launcher) {
        launcher.launch(StoredDirectoryHelper.getPicker(this));
    }

    private void requestDownloadPickVideoFolderResult(final ActivityResult result) {
        requestDownloadPickFolderResult(
                result, getString(R.string.download_path_video_key), DownloadManager.TAG_VIDEO);
    }

    private void requestDownloadPickFolderResult(final ActivityResult result,
                                                 final String key,
                                                 final String tag) {
//        Utils.logDebug(this.getClass(),result.getResultCode() + ";"+result.getData().getData().getPath());
        if (result.getResultCode() != Activity.RESULT_OK) {
            return;
        }

        if (result.getData() == null || result.getData().getData() == null) {
            showFailedDialog(R.string.general_error);
            return;
        }

        Uri uri = result.getData().getData();
        if (FilePickerActivityHelper.isOwnFileUri(this, uri)) {
            uri = Uri.fromFile(com.nononsenseapps.filepicker.Utils.getFileForUri(uri));
        } else {
            grantUriPermission(getPackageName(), uri,
                    StoredDirectoryHelper.PERMISSION_FLAGS);
        }


        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(getString(R.string.download_path_video_key), uri.toString()).apply();

        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(getString(R.string.download_path_audio_key), uri.toString()).apply();

//        Utils.logDebug(this.getClass(),"========="+uri.toString());

        if (currentDownloadYtbUrl != null) {
            downloadYoutube(currentDownloadYtbUrl);
        } else if (result_other != null && result_other.getUrl_type() == DownloadType.YOUTUBE) {
            downloadYoutube(result_other, current_video_other_site);
        } else if (result_other != null) {
            downloadOtherSite(result_other, current_video_other_site);
        }
    }

    private void showFailedDialog(@StringRes final int msg) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.general_error)
                .setMessage(msg)
                .setNegativeButton(getString(R.string.ok), null)
                .create()
                .show();
    }

    @SuppressLint("CheckResult")
    public void downloadYoutube(String url) {
        if (!checkSelectedDownloadFolder()) {
            currentDownloadYtbUrl = url;
            return;
        }

        customProgressDialog.show(this, "black", "Extract data");
        String urlExtra = url;
        if (url.contains("?list")) {
            if (url.contains("&v=")) {
                String idextra = url.split("&v=")[1];
                if (idextra.contains("&")) {
                    urlExtra = "https://m.youtube.com/watch?v=" + idextra.split("&")[0];
                } else {
                    urlExtra = "https://m.youtube.com/watch?v=" + idextra;
                }
            }
            //
        } else if (url.contains("?v=")) {
            urlExtra = url.split("&")[0];
        }
        ExtractorHelper.getStreamInfo(0, urlExtra, true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull StreamInfo result) -> {
                    this.result = result;

                    showDownloadPlanDialog();
                }, (@NonNull Throwable throwable) -> {
                    customProgressDialog.dismiss();
                });

    }


    public void downloadYoutube(StreamOtherInfo streamInfo, int position) {
        if (streamInfo == null)
            return;

        if (!checkSelectedDownloadFolder()) {
            this.result_other = streamInfo;
            this.current_video_other_site = position;
            this.currentDownloadYtbUrl = null;
            return;
        }

        this.result = streamInfo.getYtStreamInfo();
        if (AdsManager.getInstance().getClient().data.is_premium) {
            accept_download = ACCEPT_DOWNLOAD.HIGH_QUALITY;
            showDirectDialogDownload();
        } else {
            if (position == 1) {
                accept_download = ACCEPT_DOWNLOAD.LOW_QUALITY;
                if (!AdsManager.getInstance().isHaveFullAds()) {
                    showDirectDialogDownload();
                } else {
                    showInterstitialAds(new AdCloseListener() {
                        @Override
                        public void onAdClose() {
                            showDirectDialogDownload();
                        }

                        @Override
                        public void onNoAd() {
                            showDirectDialogDownload();
                        }
                    });
                }

            } else {
                accept_download = ACCEPT_DOWNLOAD.HIGH_QUALITY;

                if (AdsManager.getInstance().isHaveRewardAds()) {
                    showRewardDialog();
                } else {
                    if (AdsManager.getInstance().isHaveFullAds()) {
                        showInterstitialAds(new AdCloseListener() {
                            @Override
                            public void onAdClose() {
                                showDirectDialogDownload();
                            }

                            @Override
                            public void onNoAd() {
                                showDirectDialogDownload();
                            }
                        });
                    } else {
                        showDirectDialogDownload();
                    }

                }
            }
        }
    }

    private void showPopupTubeChange() {
        if (activityVisible) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.title_tube_change)
                    .setMessage(R.string.des_tube_change)
                    .setPositiveButton(R.string.ok, null)
                    .setCancelable(true)
                    .create();
            builder.show();
        } else
            isPendingShowTubeChange = true;

    }

    private void showDirectDialogDownload() {
        if (activityVisible) {
            if (result == null)
                return;
            List<VideoStream> sortedVideoStreams = ListHelper.getSortedStreamVideosList(this,
                    result.getVideoStreams(),
                    result.getVideoOnlyStreams(),
                    false);
            int selectedVideoStreamIndex = ListHelper.getDefaultResolutionIndex(this,
                    sortedVideoStreams);

            FragmentManager fm = getSupportFragmentManager();
            DownloadDialog downloadDialog = DownloadDialog.newInstance(this, result);
            downloadDialog.setVideoStreams(sortedVideoStreams);
            downloadDialog.setAudioStreams(result.getAudioStreams());
            downloadDialog.setSelectedVideoStream(selectedVideoStreamIndex);

            downloadDialog.setAcceptDownload(accept_download);
            downloadDialog.show(fm, "downloadDialog");
            fm.beginTransaction().commitAllowingStateLoss();
        } else {
            isPendingShowDirectDownload = true;
        }
    }

    private void showDownloadPlanDialog() {
        final SharedPreferences preferences
                = android.preference.PreferenceManager.getDefaultSharedPreferences(DownloadYtActivity.this);
        if (activityVisible) {
            customProgressDialog.dismiss();
            if (Utils.isPurchasedPremiumVersion(this)) {
                accept_download = ACCEPT_DOWNLOAD.HIGH_QUALITY;
                showDirectDialogDownload();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert);
                View root = getLayoutInflater().inflate(R.layout.layout_fab_download_dialog, null);
                builder.setTitle(R.string.title_dl_plan_dialog)
                        .setView(root)
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        })
                        .setCancelable(false);

                AlertDialog alertDialog = builder.create();

                Button btn_download_free = root.findViewById(R.id.btn_download_free);
                Button btn_download_high_quality = root.findViewById(R.id.btn_download_high_quality);
                TextView tvLowQuality = root.findViewById(R.id.tv_free_download);
                TextView tvHighQuality = root.findViewById(R.id.tv_high_quality);

                tvLowQuality.setText(R.string.normal_quality);
                tvHighQuality.setText(R.string.high_quality);
                btn_download_high_quality.setText(PurchaseUtils.getSharedInstance().getPrice(PurchaseUtils.purchase_premium));

                if (AdsManager.getInstance().isHaveRewardAds()) {
                    btn_download_free.setText(R.string.watch_video);
                } else if (AdsManager.getInstance().isHaveFullAds()) {
                    btn_download_free.setText(R.string.free_download);
                } else {
                    btn_download_free.setText(R.string.download);
                }

                btn_download_free.setOnClickListener(view -> {
                    accept_download = ACCEPT_DOWNLOAD.HIGH_QUALITY;
                    alertDialog.dismiss();

                    if (AdsManager.getInstance().isHaveRewardAds()) {
                        showRewardDialog();
                    } else {
                        if (AdsManager.getInstance().isHaveFullAds()) {
                            showInterstitialAds(new AdCloseListener() {
                                @Override
                                public void onAdClose() {
                                    showDirectDialogDownload();
                                }

                                @Override
                                public void onNoAd() {
                                    showDirectDialogDownload();
                                }
                            });
                        } else {
                            showDirectDialogDownload();
                        }

                    }
                });

                btn_download_high_quality.setOnClickListener(view -> {
                    accept_download = ACCEPT_DOWNLOAD.HIGH_QUALITY;
                    alertDialog.dismiss();

                    PurchaseUtils.getSharedInstance().purchasePremium(DownloadYtActivity.this, new PurchaseListener() {
                        @Override
                        public void purchaseFailed(String item) {
                            Toast.makeText(DownloadYtActivity.this, R.string.message_purchased_failed, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void purchaseSuccess(String item) {
                            alertDialog.dismiss();
                            Toast.makeText(DownloadYtActivity.this, R.string.restart_take_effect, Toast.LENGTH_SHORT).show();
                            preferences.edit().putBoolean(
                                    DownloadYtActivity.this.getString(R.string.show_higher_resolutions_key), true).apply();
                            ApiManager.getSharedInstance().purchase(DownloadYtActivity.this, PurchaseUtils.purchase_premium);
                        }

                        @Override
                        public void purchaseCancel(String item) {
                            Toast.makeText(DownloadYtActivity.this, R.string.message_purchased_canceled, Toast.LENGTH_SHORT).show();
                        }
                    });
                });
                alertDialog.show();

            }
        } else
            isPendingShowPlanDownload = true;
    }

    private void showRewardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View root = getLayoutInflater().inflate(R.layout.layout_reward_video, null);
        builder.setTitle("Watch ads video?")
                .setView(root)
                .setCancelable(false);
        AlertDialog alertDialog = builder.create();

        Button btn_watch_ads = root.findViewById(R.id.btn_watch_ads);
        Button btn_cancel = root.findViewById(R.id.btn_cancel);

        btn_cancel.setOnClickListener(view -> alertDialog.dismiss());
        btn_watch_ads.setOnClickListener(view -> {
            alertDialog.dismiss();
            AdsManager.getInstance().showRewardVideoAds(this, new AdRewardListener() {
                @Override
                public void onRewarded() {
                    if (activityVisible)
                        showDirectDialogDownload();
                    else
                        isPendingShowDirectDownload = true;
                }

                @Override
                public void onAdNotAvailable() {
                    showDirectDialogDownload();
                }
            });

        });
        alertDialog.show();
    }

    //show popup download for other site
    public void downloadOtherSite(StreamOtherInfo streamOtherInfo, int position) {
        if (streamOtherInfo == null)
            return;

        this.result_other = streamOtherInfo;
        current_video_other_site = position;

        if (!checkSelectedDownloadFolder()) {
            currentDownloadYtbUrl = null;
            return;
        }

        if (AdsManager.getInstance().getClient().data.is_premium) {
            showDirectDialogOtherSiteDownload();
        } else if (result_other.getUrls_stream().get(current_video_other_site).getQuality().isHDvideo()) {
            if (AdsManager.getInstance().isHaveRewardAds()) {
                showRewardOtherSiteDialog();
            } else {
                if (AdsManager.getInstance().isHaveFullAds()) {
                    showInterstitialAds(new AdCloseListener() {
                        @Override
                        public void onAdClose() {
                            showDirectDialogOtherSiteDownload();
                        }

                        @Override
                        public void onNoAd() {
                            showDirectDialogOtherSiteDownload();
                        }
                    });
                } else {
                    showDirectDialogOtherSiteDownload();
                }

            }
        } else {
            if (!AdsManager.getInstance().isHaveFullAds()) {
                showDirectDialogOtherSiteDownload();
            } else {
                showInterstitialAds(new AdCloseListener() {
                    @Override
                    public void onAdClose() {
                        showDirectDialogOtherSiteDownload();
                    }

                    @Override
                    public void onNoAd() {
                        showDirectDialogOtherSiteDownload();
                    }
                });
            }
        }


    }

    private void showRewardOtherSiteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View root = getLayoutInflater().inflate(R.layout.layout_reward_video, null);
        builder.setTitle(R.string.title_watch_ads)
                .setView(root)
                .setCancelable(false);
        AlertDialog alertDialog = builder.create();

        Button btn_watch_ads = root.findViewById(R.id.btn_watch_ads);
        Button btn_cancel = root.findViewById(R.id.btn_cancel);

        btn_cancel.setOnClickListener(view -> alertDialog.dismiss());
        btn_watch_ads.setOnClickListener(view -> {
            alertDialog.dismiss();
            AdsManager.getInstance().showRewardVideoAds(this, new AdRewardListener() {
                @Override
                public void onRewarded() {
                    showDirectDialogOtherSiteDownload();
                }

                @Override
                public void onAdNotAvailable() {
                    showDirectDialogOtherSiteDownload();
                }
            });

        });
        alertDialog.show();
    }

    private void showDirectDialogOtherSiteDownload() {
        if (activityVisible) {
            if (result_other == null)
                return;

            FragmentManager fm = getSupportFragmentManager();
            DownloadOtherDialog downloadDialog = DownloadOtherDialog.newInstance(this);
//            if (accept_download_other_site == StreamQuality.HD) {
//                downloadDialog.setInfo(result_other.getUrls_stream().get(result_other.getUrls_stream().size() - 1).getUrl_stream(), result_other.getTitle_file(), result_other.getUrl_source(), result_other.getUrl_type());
//            } else {
//
//            }
            downloadDialog.setInfo(result_other.getUrls_stream().get(current_video_other_site).getUrl_stream(), result_other.getTitle_file(), result_other.getUrl_source(), result_other.getUrl_type());

            downloadDialog.show(fm, "downloadOtherDialog");
            fm.beginTransaction().commitAllowingStateLoss();
        } else {
            isPendingShowDirectOtherSiteDownload = true;
        }
    }

    protected void fetchTiktokVideo(String url_source, FetchListener listener) {
        tiktokFetchListener = listener;
        startDownloadTiktok(url_source);
    }

    protected void testDownloadTiktok(String url_source) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert);
        View root = getLayoutInflater().inflate(R.layout.popup_server_test, null);
        builder.setTitle(R.string.title_select_server_test)
                .setView(root)
                .setNegativeButton("CANCEL", (dialogInterface, i) -> {
                })
                .setCancelable(false);
        AlertDialog alertDialog = builder.create();

        Button btn_server1 = root.findViewById(R.id.btn_server1);
        Button btn_server2 = root.findViewById(R.id.btn_server2);
        Button btn_server3 = root.findViewById(R.id.btn_server3);
        Button btn_server4 = root.findViewById(R.id.btn_server4);

        btn_server1.setOnClickListener(v ->
                {
                    alertDialog.dismiss();
                    ScriptUtils.setCurrentScript(this, 0);
                    lastTimeTiktokWebviewFinishLoad = 0l;
                    startDownloadTiktok(url_source);
                }
        );
        btn_server2.setOnClickListener(v ->
                {
                    alertDialog.dismiss();
                    ScriptUtils.setCurrentScript(this, 1);
                    lastTimeTiktokWebviewFinishLoad = 0l;
                    startDownloadTiktok(url_source);
                }
        );
        btn_server3.setOnClickListener(v ->
                {
                    alertDialog.dismiss();
                    ScriptUtils.setCurrentScript(this, 2);
                    lastTimeTiktokWebviewFinishLoad = 0l;
                    startDownloadTiktok(url_source);
                }
        );
        btn_server4.setOnClickListener(v ->
                {
                    alertDialog.dismiss();
                    ScriptUtils.setCurrentScript(this, 3);
                    lastTimeTiktokWebviewFinishLoad = 0l;
                    startDownloadTiktok(url_source);
                }
        );

        alertDialog.show();
    }

    private void preloadTittokWebview(Context context) {
        tiktokWebview = new WebView(this);
        tiktokWebview.getSettings().setJavaScriptEnabled(true);
        tiktokWebview.getSettings().setMediaPlaybackRequiresUserGesture(true);
        tiktokWebview.addJavascriptInterface(this, "browser");
        currentTiktokScript = ScriptUtils.getCurrentScript(this);

        tiktokWebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Utils.logDebug(this.getClass(), "Preload onPageFinished " + url);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isPendingLoadWebviewTiktok = false;
                        lastTimeTiktokWebviewFinishLoad = new Date().getTime();
                        if (isPedingDownloadTiktok) {
                            isPedingDownloadTiktok = false;
                            loadScriptDownloadTiktok();
                        }
                    }
                }, 500);
            }

        });
        isPendingLoadWebviewTiktok = true;
        lastTimeTiktokWebviewFinishLoad = 0l;
        tiktokWebview.loadUrl(currentTiktokScript.url_web);
    }

    private void startDownloadTiktok(String url_source) {
        current_Tiktok_Url = url_source;

        if (isPendingLoadWebviewTiktok) {
            isPedingDownloadTiktok = true;
            Utils.logDebug(this.getClass(), "Website not completed");
            return;
        }
        if (lastTimeTiktokWebviewFinishLoad == 0l) {
            isPedingDownloadTiktok = true;
            isPendingLoadWebviewTiktok = true;
            tiktokWebview.loadUrl(currentTiktokScript.url_web);
            Utils.logDebug(this.getClass(), "Website not preloaded");
            return;
        }
        if (currentTiktokScript.script_type == SCRIPT_TYPE.TIK_MATE || currentTiktokScript.script_type == SCRIPT_TYPE.SSS_TIK) {
            if (new Date().getTime() - lastTimeTiktokWebviewFinishLoad > TIME_RELOADWEBVIEW) {
                isPendingLoadWebviewTiktok = true;
                isPedingDownloadTiktok = true;
                tiktokWebview.loadUrl(currentTiktokScript.url_web);
                Utils.logDebug(this.getClass(), "Website reload by time");
                return;
            }
        }
        loadScriptDownloadTiktok();

    }

    private void loadScriptDownloadTiktok() {
        Utils.logDebug(this.getClass(), "Load script download " + currentTiktokScript.url_web);
        tiktokWebview.loadUrl(String.format(currentTiktokScript.js_script, current_Tiktok_Url));
        runnable_tikTok = new Runnable() {
            @Override
            public void run() {
                Utils.logDebug(this.getClass(), "many time request 6500ms");
                runOnUiThread(() -> {
                    if (tiktokFetchListener != null)
                        tiktokFetchListener.onFetchedFail(null);
                    if (tiktokWebview != null)
                        tiktokWebview.stopLoading();
                    changeScript();
                });
            }
        };
        handler_tikTok = new Handler(Looper.getMainLooper());
        handler_tikTok.postDelayed(runnable_tikTok, 6500);
    }

    @JavascriptInterface
    public void getData(final String json) {
        if (handler_tikTok != null && runnable_tikTok != null) {
            try {
                handler_tikTok.removeCallbacks(runnable_tikTok);
                handler_tikTok = null;
                runnable_tikTok = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if ("time_out".equals(json)) {
            runOnUiThread(() -> {
                if (tiktokFetchListener != null)
                    tiktokFetchListener.onFetchedFail(null);
                changeScript();
//                Toast.makeText(this, R.string.msg_tiktok_fail, Toast.LENGTH_SHORT).show();
            });

        } else {
            runOnUiThread(() -> {
                isPendingLoadWebviewTiktok = true;
                lastTimeTiktokWebviewFinishLoad = 0l;
                tiktokWebview.loadUrl(currentTiktokScript.url_web);
            });

            Gson gson = new GsonBuilder().create();
            try {
                TiktokResult tiktokResult = gson.fromJson(json, TiktokResult.class);
                ArrayList<VideoDetail> videos = new ArrayList<VideoDetail>();
                if (!tiktokResult.urlStream.isEmpty() && tiktokResult.urlStream.startsWith("http"))
                    videos.add(new VideoDetail(tiktokResult.urlStream, StreamQuality.HD, tiktokResult.thumb));
                if (!tiktokResult.urlStream2.isEmpty() && tiktokResult.urlStream2.startsWith("http"))
                    videos.add(new VideoDetail(tiktokResult.urlStream2, StreamQuality.HD, tiktokResult.thumb));

                StreamOtherInfo info = new StreamOtherInfo(current_Tiktok_Url, DownloadType.TIKTOK, videos, tiktokResult.title, tiktokResult.thumb);
                runOnUiThread(() -> {
                    if (tiktokFetchListener != null)
                        tiktokFetchListener.onFetchedSuccess(info);
                });
            } catch (JsonSyntaxException e) {
                runOnUiThread(() -> {
                    if (tiktokFetchListener != null)
                        tiktokFetchListener.onFetchedFail(null);
                });
            }
        }

        Utils.logDebug(this.getClass(), json);
    }

    private void changeScript() {
        Utils.logDebug(this.getClass(), "Change script");
        ScriptUtils.changeScript(this);
        currentTiktokScript = ScriptUtils.getCurrentScript(this);
        isPendingLoadWebviewTiktok = true;
        lastTimeTiktokWebviewFinishLoad = 0l;
        tiktokWebview.loadUrl(currentTiktokScript.url_web);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (tiktokWebview != null)
            tiktokWebview.destroy();

        if (handler_tikTok != null && runnable_tikTok != null) {
            handler_tikTok.removeCallbacks(runnable_tikTok);
            handler_tikTok = null;
            runnable_tikTok = null;
        }
    }
}
