package com.muicvtools.mutils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.webkit.URLUtil;

import androidx.preference.PreferenceManager;

import com.muicvtools.mutils.downloads.DownloadType;

import java.util.List;
import java.util.UUID;

public class Utils {

    public static int isDevMode(Context context) {
        if (Integer.valueOf(Build.VERSION.SDK_INT) == 16) {
            return android.provider.Settings.Secure.getInt(context.getContentResolver(),
                    android.provider.Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED, 0);
        } else if (Integer.valueOf(Build.VERSION.SDK_INT) >= 17) {
            return android.provider.Settings.Secure.getInt(context.getContentResolver(),
                    android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);
        } else return 0;
    }


    public static String isPackageInstalled(Context c, String targetPackage) {
        PackageManager pm = c.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return "0";
        }
        return "1";
    }

    public static int getNumberAppInstall(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            return packages.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getInstaller(Context context) {
        try {
            String result = context.getPackageManager().getInstallerPackageName(context.getPackageName());
            if (result != null && result.length() > 0)
                return result;
        } catch (Exception e) {
            return "";
        }
        return "";
    }

    public static int getAppBuild(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException localNameNotFoundException) {
            localNameNotFoundException.printStackTrace();
        }
        return 0;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        }
        return manufacturer + " " + model;
    }

    public static String getAppVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (pInfo.versionName != null)
                return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "unknow";
    }

    public static String getPathFolderDownloadYoutube(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String key = context.getString(R.string.download_path_video_key);
        final String downloadPath = prefs.getString(key, null);
        if (downloadPath != null)
            return Uri.decode(downloadPath);

        return null;
    }

    public static <T> void logDebug(Class<T> classType, String message) {
        if (AdsManager.DEBUG)
            Log.d(Constants.log_tag, classType.getSimpleName() + " : " + message);
    }

    public static boolean isPurchasedPremiumVersion(Context context) {
        SharedPreferences mPrefs = context.getSharedPreferences(AdsManager.PREFERENCE_NAME, Context.MODE_PRIVATE);
        if (mPrefs.getInt(PurchaseUtils.purchase_premium, 0) == 1)
            return true;

        return false;
    }

    public static boolean isPurchasedRemoveAdsVersion(Context context) {
        SharedPreferences mPrefs = context.getSharedPreferences(AdsManager.PREFERENCE_NAME, Context.MODE_PRIVATE);
        if (mPrefs.getInt(PurchaseUtils.purchase_remove_ads, 0) == 1)
            return true;
        return false;
    }

    public static String getLicenseKey(Context context) {
        SharedPreferences mPrefs = context.getSharedPreferences(AdsManager.PREFERENCE_NAME, Context.MODE_PRIVATE);
        return mPrefs.getString("uuid", "error" + UUID.randomUUID().toString());
    }

    public static void logMax(String message) {
        // Split by line, then ensure each line can fit into Log's maximum length.
        for (int i = 0, length = message.length(); i < length; i++) {
            int newline = message.indexOf('\n', i);
            newline = newline != -1 ? newline : length;
            do {
                int end = Math.min(newline, i + 3000);
                Log.d(Constants.log_tag, message.substring(i, end));
                i = end;
            } while (i < newline);
        }
    }

    public static DownloadType getDownloadType(String urlString) {
        if (urlString == null)
            return DownloadType.OTHER;
        if (TextUtils.isEmpty(urlString))
            return DownloadType.OTHER;

        if (URLUtil.isValidUrl(urlString) && Patterns.WEB_URL.matcher(urlString).matches()) {
            if ((urlString.contains("facebook.com") && !urlString.contains("instagram.com")) || urlString.contains("facebook.com") || urlString.contains("fb.watch") || urlString.contains("fb.gg"))
                return DownloadType.FACEBOOK;
            if (urlString.contains("instagram.com") || ( urlString.contains("facebook.com") && urlString.contains("instagram.com")))
                return DownloadType.INSTA;
            if (urlString.contains("twitter.com"))
                return DownloadType.TWITTER;
            if (urlString.contains("tiktok.com") || urlString.contains("douyin.com"))
                return DownloadType.TIKTOK;
            if (urlString.contains("youtube.com") || urlString.contains("youtu.be")) {
                if (!urlString.endsWith("youtube.com") && !urlString.endsWith("youtube.com/"))
                    return DownloadType.YOUTUBE;
            }
            if (urlString.contains("vimeo.com") || urlString.contains("vimeopro.com"))
                return DownloadType.VIMEO;
            if (urlString.contains("dailymotion.com") || urlString.contains("dai.ly"))
                return DownloadType.DAILYMOTION;

            if (urlString.contains("pinterest.com/pin") || urlString.contains("pin.it/"))
                return DownloadType.PINTEREST;
        }
        return DownloadType.OTHER;
    }

//    public static boolean checkInternetConnection(Context context) {
//
//        ConnectivityManager con_manager = (ConnectivityManager)
//                context.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        return (con_manager.getActiveNetworkInfo() != null
//                && con_manager.getActiveNetworkInfo().isAvailable()
//                && con_manager.getActiveNetworkInfo().isConnected());
//    }

//    public static String getAppSignature(Context context) {
//        try {
//            Signature[] sigs = new Signature[0];
//            sigs = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES).signatures;
//
//            for (Signature sig : sigs) {
//                logDebug(context.getClass(), sig.toCharsString() + ";" + sig.toString() + ";" + sig.);
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//
//        }
//        return "error";
//
//    }
}
