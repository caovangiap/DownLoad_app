package com.muicvtools.mutils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiManager {
    private static ApiManager sharedInstance;
    private final static String SERVER_URL_CONFIG = "url_client_config";

    public static ApiManager getSharedInstance() {
        if (sharedInstance == null) {
            sharedInstance = new ApiManager();
        }
        return sharedInstance;
    }

    private void fakeClientConfig(com.muicvtools.mutils.ClientConfig clientConfig) {
        clientConfig.data.is_accept = true;
        clientConfig.data.id_banner_ad = "ca-app-pub-3940256099942544/6300978111";
        clientConfig.data.id_full_ad = "ca-app-pub-3940256099942544/1033173712";
        clientConfig.data.id_reward_ad = "ca-app-pub-3940256099942544/5224354917";
        clientConfig.data.id_open_ad = "ca-app-pub-3940256099942544/3419835294";
        clientConfig.data.id_banner_ad_home = "ca-app-pub-3940256099942544/6300978111";
        clientConfig.data.delay_show_openads = 10;
        Utils.logDebug(this.getClass(), "fakeClientConfig");
    }

    private String getServerUrl() {
        return new String(Base64.decode(Constants.CODE_SERVER_NEW, Base64.DEFAULT));
    }

    private void changeServerUrl(SharedPreferences mPrefs) {
        int server_int = mPrefs.getInt(SERVER_URL_CONFIG, 1);
        if (server_int == 1)
            server_int = 2;
        else
            server_int = 1;

        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt(SERVER_URL_CONFIG, server_int).apply();
    }

    public com.muicvtools.mutils.ClientConfig loadOffineConfig(SharedPreferences mPrefs) {
        if (!mPrefs.contains(Constants.tag_data)) {
            return null;
        }
        Gson gson = new GsonBuilder().create();
        com.muicvtools.mutils.ClientConfig clientConfig;
        try {
            clientConfig = gson.fromJson(mPrefs.getString(Constants.tag_data, "error"), com.muicvtools.mutils.ClientConfig.class);
        } catch (Exception e) {
            mPrefs.edit().remove(Constants.tag_data).apply();
            return null;
        }

        if (AdsManager.DEBUG) {
            fakeClientConfig(clientConfig);
        }

        Utils.logDebug(this.getClass(), "Get config off");
        return clientConfig;
    }

    private String readLicensekey() {
        File documentFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "");
        if (documentFolder.exists() && documentFolder.listFiles() != null) {
            for (File file : documentFolder.listFiles()) {
                if (file.isDirectory() && file.getName() != null && (file.getName().endsWith(".license") || (file.getName().endsWith(".licenses")))) {
                    String licenseKey = "";
                    if (file.getName().endsWith(".license"))
                        licenseKey = file.getName().replace(".license", "");
                    else {
                        licenseKey = file.getName().replace(".licenses", "");
                    }
                    Utils.logDebug(this.getClass(), "Old license key = " + licenseKey);
                    return licenseKey;
                }
            }
        }
        Utils.logDebug(this.getClass(), "Old license key not exits");
        return null;
    }

    private void writeLicensekey(String license) {
        File documentFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "");
        if (!documentFolder.exists()) {
            boolean isCreate = documentFolder.mkdir();
            if (!isCreate) {
                Utils.logDebug(this.getClass(), "Write license fail!");
                return;
            }
        }

        if (documentFolder.listFiles() != null) {
            for (File file : documentFolder.listFiles()) {
                if (file.isDirectory() && file.getName() != null && (file.getName().endsWith(".license") || (file.getName().endsWith(".licenses")))) {
                    Utils.logDebug(this.getClass(), "License key exists, Create license fail!");
                    return;
                }
            }
        }

        File licenseFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), license + ".license");
        boolean isCreate = licenseFile.mkdir();
        if (isCreate)
            Utils.logDebug(this.getClass(), "Create license success!");
        else
            Utils.logDebug(this.getClass(), "Create license fail!");
    }

    private void updateLicenseKeyWhenPurchased(String license) {
        File documentFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "");
        if (!documentFolder.exists()) {
            boolean isCreate = documentFolder.mkdir();
            if (!isCreate) {
                Utils.logDebug(this.getClass(), "Write license fail!");
                return;
            }
        }

        if (documentFolder.listFiles() != null) {
            for (File file : documentFolder.listFiles()) {
                if (file.isDirectory() && file.getName() != null && file.getName().endsWith(".license")) {
                    boolean isDelete = file.delete();
                    if (isDelete)
                        Utils.logDebug(this.getClass(), "Delete current regular license key success");
                    else
                        Utils.logDebug(this.getClass(), "Delete current regular license key fail!");
                    if (!isDelete)
                        return;

                    File licenseFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), license + ".licenses");
                    boolean isUpdateLicense = licenseFile.mkdir();
                    if (isUpdateLicense)
                        Utils.logDebug(this.getClass(), "Update premium license key success");
                    else
                        Utils.logDebug(this.getClass(), "Update premium license key fail!");
                    return;
                }
                if (file.isDirectory() && file.getName() != null && file.getName().endsWith(".licenses")) {
                    return;
                }
            }
        }

        File licenseFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), license + ".licenses");
        boolean isUpdateLicense = licenseFile.mkdir();
        if (isUpdateLicense)
            Utils.logDebug(this.getClass(), "Create premium license key success");
        else
            Utils.logDebug(this.getClass(), "Create premium license key fail!");

    }

    public void restorePremiumLicense(Context context, LicenseListener listener) {
        File documentFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "");
        if (documentFolder.exists() && documentFolder.listFiles() != null) {
            for (File file : documentFolder.listFiles()) {
                if (file.isDirectory() && file.getName() != null && (file.getName().endsWith(".licenses"))) {
                    Utils.logDebug(this.getClass(), "Restore premium license key success");
                    SharedPreferences mPrefs = context.getSharedPreferences(AdsManager.PREFERENCE_NAME, Context.MODE_PRIVATE);
                    mPrefs.edit().putInt(PurchaseUtils.purchase_premium, 1).apply();

                    if (listener != null)
                        listener.onLicenseSuccess();
                    return;
                }
            }
        }

        Utils.logDebug(this.getClass(), "Restore premium license key fail");
        if (listener != null)
            listener.onLicenseFail();
    }

    public void getConfig(Context context, GetConfiglistener getConfiglistener) {
        SharedPreferences mPrefs = context.getSharedPreferences(AdsManager.PREFERENCE_NAME, Context.MODE_PRIVATE);

        boolean isUpdate = mPrefs.contains(Constants.tag_data);
        String uuid;
        if (mPrefs.contains("uuid")) {
            uuid = mPrefs.getString("uuid", "error" + UUID.randomUUID().toString());
            if (readLicensekey() == null && isUpdate) {
                writeLicensekey(uuid);
            }
        } else {
            uuid = readLicensekey();
            if (uuid == null) {
                uuid = UUID.randomUUID().toString();
            } else {
                isUpdate = true;
            }
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString("uuid", uuid).apply();
        }

        int numberPhoto, number_download, number_picture;
        numberPhoto = number_picture = number_download = 0;

        File dcim = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        if (dcim != null && dcim.listFiles() != null)
            numberPhoto = dcim.listFiles().length;

        dcim = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");
        if (dcim != null && dcim.listFiles() != null)
            number_download = dcim.listFiles().length;

        dcim = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "");
        if (dcim != null && dcim.listFiles() != null)
            number_picture = dcim.listFiles().length;

        String isCharging, pin_ac;
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        if (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL) {
            isCharging = "1";
            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB)
                pin_ac = "usb";
            else if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC)
                pin_ac = "ac";
            else
                pin_ac = "";
        } else {
            isCharging = "0";
            pin_ac = "";
        }

        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            locale = context.getResources().getConfiguration().locale;
        }
        if (!isUpdate && !AdsManager.DEBUG) {
            if (!"com.android.vending".equals(Utils.getInstaller(context)) || ("1".equals(isCharging) && "usb".equals(pin_ac)) || (Utils.isDevMode(context) == 1 && "1".equals(isCharging))) {
                if (getConfiglistener != null) {
                    getConfiglistener.onGetConfigOnline(new ClientConfig());
                }
                Utils.logDebug(this.getClass(), "No getconfig");
                return;
            }
        }

        getConfigOnline(mPrefs, getConfiglistener, uuid, Utils.getAppBuild(context), Utils.getDeviceName(), locale.getLanguage().toLowerCase(), locale.getCountry().toLowerCase(), isUpdate, Utils.isDevMode(context), Utils.getInstaller(context), isCharging, pin_ac, numberPhoto, number_picture, number_download);

//        getConfigOnline(mPrefs, getConfiglistener, uuid, context.getPackageName(), mPrefs.getInt(PurchaseUtils.purchase_premium, 0),
//                isUpdate, Utils.getDeviceName(), locale.getLanguage().toLowerCase(), locale.getCountry().toLowerCase(), Utils.isDevMode(context),
//                Utils.getInstaller(context), isCharging, pin_ac, numberPhoto, number_picture, number_download);
    }

    private void getConfigOnline(SharedPreferences mPrefs, GetConfiglistener getConfiglistener, String uuid, int build, String device, String lg, String lc, Boolean isUpdate, int isDeveloper, String installer, String pin_status, String pin_ac, int numPhoto, int numPicture, int numDownload /*SharedPreferences mPrefs, GetConfiglistener getConfiglistener, String uuid, String id_app, int premium, Boolean isUpdate, String device, String lg, String lc, int isDeveloper,
                                 String installer, String pin_status, String pin_ac, int numPhoto, int numPicture, int numDownload*/) {

        Utils.logDebug(this.getClass(), "Url = " + getServerUrl());

        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("pid", Constants.PID)
                .add("id", uuid)
                .add("build", build + "")
                .add("is_update", String.valueOf(isUpdate))
                .add("device", device)
                .add("lg", lg)
                .add("lc", lc)
                .add("is_developer", isDeveloper + "")
                .add("installer", installer)
                .add("battery_status", pin_status)
                .add("battery_ac", pin_ac)
                .add("num_photo", numPhoto + "")
                .add("num_picture", numPicture + "")
                .add("num_download", numDownload + "")
                .build();

        Request okRequest = new Request.Builder()
                .url(getServerUrl())
                .post(formBody)
                .build();
        client.newCall(okRequest).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                getConfiglistener.onGetConfigFail();
//                errorConnection(mPrefs, getConfiglistener, uuid, id_app, premium, isUpdate, device, lg, lc, isDeveloper, installer, pin_status, pin_ac, numPhoto, numPicture, numDownload);
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.code() == 200) {
                    Gson gson = new GsonBuilder().create();
                    String result = response.body().string();
                    if (result == null || "".equals(result)) {
                        return;
                    }
                    com.muicvtools.mutils.ClientConfig clientConfig;
                    try {
                        clientConfig = gson.fromJson(result, com.muicvtools.mutils.ClientConfig.class);
                    } catch (Exception e) {
                        getConfiglistener.onGetConfigFail();
//                        connectionServer(mPrefs, getConfiglistener, uuid, id_app, premium, isUpdate, device, lg, lc, isDeveloper, installer, pin_status, pin_ac, numPhoto, numPicture, numDownload);
                        return;
                    }

                    if (AdsManager.DEBUG) {
                        fakeClientConfig(clientConfig);
                    }

                    SharedPreferences.Editor editor = mPrefs.edit();
                    if (clientConfig.data.is_accept == true) {
                        if (!isUpdate)
                            writeLicensekey(uuid);
                        editor.putString(Constants.tag_data, result);
                    }
                    if (clientConfig.data.is_premium == true)
                        editor.putInt(PurchaseUtils.purchase_premium, 1);
                    editor.apply();

                    if (getConfiglistener != null) {
                        getConfiglistener.onGetConfigOnline(clientConfig);

                    }

                } else {
                    getConfiglistener.onGetConfigFail();
                }

            }
        });
    }

//    private void connectionServer(SharedPreferences mPrefs, GetConfiglistener getConfiglistener, String uuid, String id_app, int premium, Boolean isUpdate, String device, String lg, String lc, int isDeveloper,
//                                  String installer, String pin_status, String pin_ac, int numPhoto, int numPicture, int numDownload) {
//
//        getConfigOnline(mPrefs, getConfiglistener, uuid, id_app, premium,
//                isUpdate, device, lg, lc, isDeveloper,
//                installer, pin_status, pin_ac, numPhoto, numPicture, numDownload);
//    }

    public void addToHistory(Context context, String title, String url, String type) {
        SharedPreferences prefs = context.getSharedPreferences(AdsManager.PREFERENCE_NAME, Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String current_history = prefs.getString("history", "error");
        com.muicvtools.mutils.HistoryResult historyResult;
        try {
            historyResult = gson.fromJson(current_history, com.muicvtools.mutils.HistoryResult.class);
        } catch (Exception e) {
            historyResult = new com.muicvtools.mutils.HistoryResult();
            historyResult.history = new ArrayList<com.muicvtools.mutils.History>();
        }

        if (historyResult.history.size() >= 20)
            historyResult.history.remove(0);

        historyResult.history.add(new com.muicvtools.mutils.History(title, url, type, new com.muicvtools.mutils.GsonDate(new Date().getTime())));


        SharedPreferences.Editor editor = prefs.edit();
        String json = gson.toJson(historyResult);
        editor.putString("history", json);
        editor.apply();
    }

    public void getHistory(Context context, com.muicvtools.mutils.HistoryListener listener) {
        SharedPreferences prefs = context.getSharedPreferences(AdsManager.PREFERENCE_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("history", "error");
        com.muicvtools.mutils.HistoryResult historyResult;
        try {
            historyResult = gson.fromJson(json, com.muicvtools.mutils.HistoryResult.class);
        } catch (Exception e) {
            historyResult = new com.muicvtools.mutils.HistoryResult();
            historyResult.history = new ArrayList<com.muicvtools.mutils.History>();
        }
        listener.getHistorySuccess(historyResult);
    }

    public void getNotify(Context context, NotifyListener listener) {
        SharedPreferences mPrefs = context.getSharedPreferences(AdsManager.PREFERENCE_NAME, Context.MODE_PRIVATE);
        String url_client_config = getServerUrl();
        url_client_config += "notify";

        String is_accept = "0";
        if (mPrefs.contains(Constants.tag_data))
            is_accept = "1";

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("id_app", context.getPackageName())
                .add("is_accept", is_accept)
                .build();
        Request okRequest = new Request.Builder()
                .url(url_client_config)
                .post(body)
                .build();
        client.newCall(okRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                e.printStackTrace();
                if (listener != null)
                    listener.getNotifyFail();

            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {

                    Gson gson = new GsonBuilder().setDateFormat(DateFormat.LONG).create();
                    String result = response.body().string();
                    com.muicvtools.mutils.NotifyResult result1 = gson.fromJson(result, com.muicvtools.mutils.NotifyResult.class);
                    if (listener != null)
                        listener.getNotifySuccess(result1);
                    Log.d(Constants.log_tag, "getNotify done!");
                } else {
                    if (listener != null)
                        listener.getNotifyFail();
                }
            }
        });
    }

    public void activeCode(Context context, String code, ActiveCodeListener listener) {
        SharedPreferences mPrefs = context.getSharedPreferences(AdsManager.PREFERENCE_NAME, Context.MODE_PRIVATE);
        String url_client_config = getServerUrl();
        url_client_config += "active_code";

        String uuid = mPrefs.getString("uuid", "error" + UUID.randomUUID().toString());

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("id", uuid)
                .add("code", code)
                .add("id_app", context.getPackageName())
                .build();
        Request okRequest = new Request.Builder()
                .url(url_client_config)
                .post(body)
                .build();
        client.newCall(okRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                e.printStackTrace();
                if (listener != null)
                    listener.onActiveCodeInvalidCode();
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (listener != null)
                        listener.onActiveCodeSuccess();
                    Log.d(Constants.log_tag, "activeCode done!");
                } else {
                    if (listener != null) {
                        if (response.code() == 403)
                            listener.onActiveCodeTimeFail();
                        else
                            listener.onActiveCodeInvalidCode();
                    }
                }

            }
        });
    }

    public void license(Context context, String license, LicenseListener listener) {
        SharedPreferences mPrefs = context.getSharedPreferences(AdsManager.PREFERENCE_NAME, Context.MODE_PRIVATE);
        String url_client_config = getServerUrl();
        String uuid = mPrefs.getString("uuid", "error" + UUID.randomUUID().toString());

        url_client_config += "license";

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("id", uuid)
                .add("license", license)
                .add("id_app", context.getPackageName())
                .build();
        Request okRequest = new Request.Builder()
                .url(url_client_config)
                .post(body)
                .build();
        client.newCall(okRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                e.printStackTrace();
                if (listener != null)
                    listener.onLicenseFail();
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (listener != null)
                        listener.onLicenseSuccess();
                    Log.d(Constants.log_tag, "license done!");
                } else {
                    listener.onLicenseFail();
                }

            }

        });
    }

    public void purchase(Context context, String item) {

        SharedPreferences mPrefs = context.getSharedPreferences(AdsManager.PREFERENCE_NAME, Context.MODE_PRIVATE);
        String url_client_config = getServerUrl();
        url_client_config += "purchase";

        String uuid = mPrefs.getString("uuid", "error" + UUID.randomUUID().toString());

        if (PurchaseUtils.purchase_premium.equals(item))
            updateLicenseKeyWhenPurchased(uuid);

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("id", uuid)
                .add("id_app", context.getPackageName())
                .add("item", item)
                .build();
        Request okRequest = new Request.Builder()
                .url(url_client_config)
                .post(body)
                .build();
        client.newCall(okRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d(Constants.log_tag, "purchase done!");
                }
            }
        });
    }

    public void getAds(Context context, String type_app, com.muicvtools.mutils.GetAdsListener listener) {
        SharedPreferences mPrefs = context.getSharedPreferences(AdsManager.PREFERENCE_NAME, Context.MODE_PRIVATE);
        String url_client_config = getServerUrl();

        url_client_config += "get_ads";

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("type_app", type_app)
                .build();
        Request okRequest = new Request.Builder()
                .url(url_client_config)
                .post(body)
                .build();
        client.newCall(okRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                e.printStackTrace();
                if (listener != null)
                    listener.getAdsFail();
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.code() == 200) {
                    Gson gson = new GsonBuilder().create();
                    String result = response.body().string();
                    com.muicvtools.mutils.GetAdsResult result1 = gson.fromJson(result, com.muicvtools.mutils.GetAdsResult.class);
                    if (listener != null)
                        listener.getAdsSuccess(result1);
                    Utils.logDebug(this.getClass(), "Get ads success");
                } else {
                    if (listener != null)
                        listener.getAdsFail();
                    Utils.logDebug(this.getClass(), "Get ads fail");
                }
            }

        });
    }
}
