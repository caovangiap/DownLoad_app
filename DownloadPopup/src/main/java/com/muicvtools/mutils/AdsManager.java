package com.muicvtools.mutils;

import static androidx.lifecycle.Lifecycle.Event.ON_START;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.Random;

public class AdsManager implements Application.ActivityLifecycleCallbacks, LifecycleObserver {
    public static boolean DEBUG = false;
    public static String PREFERENCE_NAME = "DEFAULT_NAME";
    public static String APPLICATION_ID = "APPLICATION_ID";

    private final Application myApplication;
    private static AdsManager sharedInstance;

    private Activity currentActivity;


    private AdmobManager admobManager;
    private ApplovinManager applovinManager;
    private ClientConfig clientConfig;

    public AdsManager(Application myApplication) {
        this.myApplication = myApplication;

//        this.myApplication.registerActivityLifecycleCallbacks(this);
//        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);


        sharedInstance = this;

        if (clientConfig == null) {
            SharedPreferences mPrefs = myApplication.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            clientConfig = ApiManager.getSharedInstance().loadOffineConfig(mPrefs);
            if (mPrefs.contains(PurchaseUtils.purchase_premium) || mPrefs.contains(PurchaseUtils.purchase_remove_ads) || mPrefs.getInt("no_ads", 0) == 1 || mPrefs.getInt("full_version", 0) == 1)
                clientConfig.data.is_premium = true;
        }
        Utils.logDebug(this.getClass(), "AdsManager init application");
    }

    public static AdsManager getInstance() {
        return sharedInstance;
    }

    public ClientConfig getClient() {
        return clientConfig;
    }

    public void fakeClientConfig(ClientConfig client) {
        clientConfig = client;
    }

    public TYPE_BANNER getTypeOfBanner() {
        if (clientConfig == null || clientConfig.data.is_premium) {
            return TYPE_BANNER.NONE;
        }

        boolean is_admob_ads = new Random().nextInt(100) < clientConfig.data.daily_percent_admob;
        if (is_admob_ads) {
            if (clientConfig.data.id_banner_ad != null && !"".equals(clientConfig.data.id_banner_ad))
                return TYPE_BANNER.ADMOB;
            if (clientConfig.data.id_banner_med != null && !"".equals(clientConfig.data.id_banner_med))
                return TYPE_BANNER.MEDIATION;
            return TYPE_BANNER.NONE;

        } else {
            if (clientConfig.data.id_banner_med != null && !"".equals(clientConfig.data.id_banner_med))
                return TYPE_BANNER.MEDIATION;
            if (clientConfig.data.id_banner_ad != null && !"".equals(clientConfig.data.id_banner_ad))
                return TYPE_BANNER.ADMOB;
            return TYPE_BANNER.NONE;
        }
    }

    public void loadAds(Activity context, ClientConfig client) {
        clientConfig = client;
        currentActivity = context;

        SharedPreferences mPrefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        if (mPrefs.contains(PurchaseUtils.purchase_premium) || mPrefs.contains(PurchaseUtils.purchase_remove_ads) || mPrefs.getInt("no_ads", 0) == 1 || mPrefs.getInt("full_version", 0) == 1)
            clientConfig.data.is_premium = true;

        Utils.logDebug(this.getClass(), "loadAds Debug = " + DEBUG);
        if (clientConfig == null || clientConfig.data.is_premium || context == null) {
            return;
        }
        loadAdmobAds(context);
        loadMedationAds(context);
    }

    private void loadAdmobAds(Activity context) {
        admobManager = new AdmobManager(this.myApplication);
        AdmobManager.DEBUG = DEBUG;
        AdmobManager.PREFERENCE_NAME = PREFERENCE_NAME;
        AdmobManager.APPLICATION_ID = APPLICATION_ID;
        admobManager.loadAds(context, clientConfig);
    }

    private void loadMedationAds(Activity context) {
        applovinManager = new ApplovinManager(this.myApplication);
        ApplovinManager.DEBUG = DEBUG;
        ApplovinManager.PREFERENCE_NAME = PREFERENCE_NAME;
        ApplovinManager.APPLICATION_ID = APPLICATION_ID;
        applovinManager.loadAds(context, clientConfig);
    }

    public void showRewardVideoAds(Activity activity, AdRewardListener adRewardListener) {
        boolean is_admob_ads = new Random().nextInt(100) < clientConfig.data.daily_percent_admob;
        if (is_admob_ads) {
            if (admobManager != null && admobManager.isHaveRewardAds())
                admobManager.showRewardVideoAds(activity, adRewardListener);
            else if (applovinManager != null && applovinManager.isHaveRewardAds())
                applovinManager.showRewardVideoAds(adRewardListener);
            else if (adRewardListener != null)
                adRewardListener.onAdNotAvailable();
        } else {
            if (applovinManager != null && applovinManager.isHaveRewardAds())
                applovinManager.showRewardVideoAds(adRewardListener);
            else if (admobManager != null && admobManager.isHaveRewardAds())
                admobManager.showRewardVideoAds(activity, adRewardListener);
            else if (adRewardListener != null)
                adRewardListener.onAdNotAvailable();
        }
    }


    public boolean isHaveFullAds() {
        if (admobManager != null && admobManager.isHaveFullAds())
            return true;
        if (applovinManager != null && applovinManager.isHaveFullAds())
            return true;
        return false;
    }

    public boolean isHaveFullAdsWatchVideo() {
        if (isHaveFullAds() && clientConfig.data.is_ads_watch_video) {
            return true;
        }
        return false;
    }

    public boolean isHaveRewardAds() {
        if (admobManager != null && admobManager.isHaveRewardAds())
            return true;
        if (applovinManager != null && applovinManager.isHaveRewardAds())
            return true;
        return false;
    }

    public void showInterstitialAds(Activity currentActivity, AdCloseListener adCloseListener) {
        boolean is_admob_ads = new Random().nextInt(100) < clientConfig.data.daily_percent_admob;
        if (is_admob_ads) {
            if (admobManager != null && admobManager.isHaveFullAds())
                admobManager.showInterstitialAds(currentActivity, adCloseListener);
            else if (applovinManager != null && applovinManager.isHaveFullAds())
                applovinManager.showInterstitialAds(adCloseListener);
            else if (adCloseListener != null)
                adCloseListener.onNoAd();
        } else {
            if (applovinManager != null && applovinManager.isHaveFullAds())
                applovinManager.showInterstitialAds(adCloseListener);
            else if (admobManager != null && admobManager.isHaveFullAds())
                admobManager.showInterstitialAds(currentActivity, adCloseListener);
            else if (adCloseListener != null)
                adCloseListener.onNoAd();
        }
    }

    /**
     * Shows the ad if one isn't already showing.
     */
    public void showOpenAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (clientConfig == null || clientConfig.data.is_premium == true || admobManager == null || applovinManager == null ||
                currentActivity == null) {
            Utils.logDebug(this.getClass(), "Can not show open ads");
            return;
        }

        if (admobManager != null && admobManager.getIsShowingFullScreenAds()) {
            Utils.logDebug(this.getClass(), "Admob currently showing. Can not show open ad");
            return;
        }

        if (applovinManager != null && applovinManager.getIsShowingFullScreenAds()) {
            Utils.logDebug(this.getClass(), "Applovin currently showing. Can not show open ad");
            return;
        }

        if (!canShowOpen()) {
            Utils.logDebug(this.getClass(), "notShowOpen = true -> Can not show open ad");
            return;
        }
        if (currentActivity instanceof AdOverlayActivity) {
            Utils.logDebug(this.getClass(), "Will show open ad");
            AdOverlayActivity activity = (AdOverlayActivity) currentActivity;
            if (!activity.isShowOpenAds())
                return;

            boolean is_admob_ads = new Random().nextInt(100) < clientConfig.data.daily_percent_admob;
            if (is_admob_ads) {

                if (admobManager.isOpenAdAvailable())
                    admobManager.showOpenAds();
                else if (applovinManager.isOpenAdAvailable())
                    applovinManager.showOpenAds();
            } else {

                if (applovinManager.isOpenAdAvailable())
                    applovinManager.showOpenAds();
                else if (admobManager.isOpenAdAvailable())
                    admobManager.showOpenAds();
            }
        }

    }

    private boolean canShowOpen() {
        if (currentActivity == null)
            return false;
        SharedPreferences mPrefs = currentActivity.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        boolean isNotShowOpen = mPrefs.getBoolean("notShowOpen", false);
        if (isNotShowOpen) {
            Utils.logDebug(this.getClass(), "Can not show ad --- notShowopen = " + isNotShowOpen);
            mPrefs.edit().remove("notShowOpen").apply();
            return false;
        } else
            return true;
    }

    public void setDoNotShowOpen() {
        if (currentActivity != null) {
            SharedPreferences mPrefs = currentActivity.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            mPrefs.edit().putBoolean("notShowOpen", true).apply();
        }
    }


    private void setCurrentActivity(Activity activity) {
        currentActivity = activity;
        if (admobManager != null)
            admobManager.setCurrentActivity(activity);
        if (applovinManager != null)
            applovinManager.setCurrentActivity(activity);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        setCurrentActivity(activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        setCurrentActivity(activity);
        Utils.logDebug(this.getClass(), "onActivityStarted " + activity.getLocalClassName());
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        setCurrentActivity(activity);
        Utils.logDebug(this.getClass(), "onActivityResumed " + activity.getLocalClassName());
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        Utils.logDebug(this.getClass(), "onActivityPaused " + activity.getLocalClassName());
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        Utils.logDebug(this.getClass(), "onActivityStopped " + activity.getLocalClassName());
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        Utils.logDebug(this.getClass(), "onActivitySaveInstanceState " + activity.getLocalClassName());

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        Utils.logDebug(this.getClass(), "onActivityDestroyed " + activity.getLocalClassName());
    }

    @OnLifecycleEvent(ON_START)
    public void onStart() {
        showOpenAdIfAvailable();
        Utils.logDebug(this.getClass(), "onStart");
    }

}

//
//import static androidx.lifecycle.Lifecycle.Event.ON_START;
//
//import android.app.Activity;
//import android.app.Application;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.lifecycle.LifecycleObserver;
//import androidx.lifecycle.OnLifecycleEvent;
//import androidx.lifecycle.ProcessLifecycleOwner;
//
//import com.applovin.mediation.MaxAd;
//import com.applovin.mediation.MaxAdListener;
//import com.applovin.mediation.MaxError;
//import com.applovin.mediation.MaxReward;
//import com.applovin.mediation.MaxRewardedAdListener;
//import com.applovin.mediation.ads.MaxInterstitialAd;
//import com.applovin.mediation.ads.MaxRewardedAd;
//import com.applovin.sdk.AppLovinSdk;
//import com.applovin.sdk.AppLovinSdkConfiguration;
//import com.google.android.gms.ads.MobileAds;
//
//import java.util.Date;
//
//public class AdsManager implements Application.ActivityLifecycleCallbacks, LifecycleObserver {
//    private static AdsManager sharedInstance;
//    private final Application myApplication;
//    private static ClientConfig clientConfig;
//
//    private MaxInterstitialAd interstitialAd;
//    private MaxRewardedAd rewardedAd;
//
//    private Date lastTimeShowInterstitialAds;
//    private Date lastTimeShowRewardAds;
//
//    private int count_load_full_fail = 0;
//    private int count_load_reward_fail = 0;
//    private int count_max_load_retry = 5;
//
//    private int count_show_reward_completed = 0;
//    private int count_show_full_completed = 0;
//
//    private Activity currentActivity;
//    private boolean isShowingFullScreenAds = false;
//    private AdRewardListener adRewardListener;
//    private AdCloseListener adCloseListener;
//
//    //public variable
//    public static boolean DEBUG = false;
//    public static String PREFERENCE_NAME = "DEFAULT_NAME";
//    public static String APPLICATION_ID = "APPLICATION_ID";
//
//    public AdsManager(Application myApplication) {
//        this.myApplication = myApplication;
//        this.myApplication.registerActivityLifecycleCallbacks(this);
//        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
//        sharedInstance = this;
//
//        if (clientConfig == null) {
//            SharedPreferences mPrefs = myApplication.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
//            clientConfig = ApiManager.getSharedInstance().loadOffineConfig(mPrefs);
//            if (mPrefs.contains(PurchaseUtils.purchase_premium) || mPrefs.contains(PurchaseUtils.purchase_remove_ads) || mPrefs.getInt("no_ads", 0) == 1 || mPrefs.getInt("full_version", 0) == 1)
//                clientConfig.is_premium = 1;
//        }
//
//        Utils.logDebug(this.getClass(), "AdsManager init application");
//    }
//
//    public static AdsManager getInstance() {
//        return sharedInstance;
//    }
//
//    public ClientConfig getClient() {
//        return clientConfig;
//    }
//
//    public void fakeClientConfig(ClientConfig client) {
//        clientConfig = client;
//    }
//
//    public void loadAds(Context context, ClientConfig client) {
//        clientConfig = client;
//        SharedPreferences mPrefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
//        if (mPrefs.contains(PurchaseUtils.purchase_premium) || mPrefs.contains(PurchaseUtils.purchase_remove_ads) || mPrefs.getInt("no_ads", 0) == 1 || mPrefs.getInt("full_version", 0) == 1)
//            clientConfig.is_premium = 1;
//
//        Utils.logDebug(this.getClass(), "loadAds Debug = " + DEBUG);
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1 || clientConfig.is_noti == 3) {
//            return;
//        }
//        if (clientConfig.id_full_ad != null && !"".equals(clientConfig.id_full_ad) ) { //|| !"".equals(clientConfig.id_reward_ad)
//            resetNumberAds();
//
//            if(clientConfig.id_full_ad.contains("ca-app"))
//            {
//                MobileAds.initialize(myApplication, initializationStatus -> {
//                    startPreloadAds();
//                });
//            }
//            else
//            {
//                AppLovinSdk.getInstance(context).setMediationProvider("max");
//                AppLovinSdk.initializeSdk(context, new AppLovinSdk.SdkInitializationListener() {
//                    @Override
//                    public void onSdkInitialized(final AppLovinSdkConfiguration configuration) {
//                        Utils.logDebug(AdsManager.class, "Mediation is successfully initialized.");
//                        startPreloadAds();
//                    }
//                });
//            }
//        }
//    }
//
//    private void resetNumberAds() {
//        count_show_full_completed = 0;
//        count_show_reward_completed = 0;
//    }
//
//    private void startPreloadAds() {
//        if (clientConfig.total_full_admob == 0)
//            clientConfig.total_full_admob = 1;
//        if (clientConfig.total_reward_admob == 0)
//            clientConfig.total_reward_admob = 1;
//
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1 || clientConfig.is_noti == 3 || currentActivity == null) {
//            return;
//        }
//
//        interstitialAd = null;
//        rewardedAd = null;
//        //init interstitial
//        if (clientConfig.id_full_ad != null && !"".equals(clientConfig.id_full_ad)) {
//            interstitialAd = new MaxInterstitialAd(clientConfig.id_full_ad, currentActivity);
//            interstitialAd.setListener(new MaxAdListener() {
//                @Override
//                public void onAdLoaded(MaxAd maxAd) {
//                    count_load_full_fail = 0;
//                }
//
//                @Override
//                public void onAdDisplayed(MaxAd maxAd) {
//                    count_show_full_completed++;
//
//                    isShowingFullScreenAds = true;
//                    lastTimeShowInterstitialAds = new Date();
//                }
//
//                @Override
//                public void onAdHidden(MaxAd maxAd) {
//                    isShowingFullScreenAds = false;
//                    if (adCloseListener != null)
//                        adCloseListener.onAdClose();
//                    loadInterstitialAds();
//                }
//
//                @Override
//                public void onAdClicked(MaxAd maxAd) {
//
//                }
//
//                @Override
//                public void onAdLoadFailed(String s, MaxError maxError) {
//                    count_load_full_fail++;
//                }
//
//                @Override
//                public void onAdDisplayFailed(MaxAd maxAd, MaxError maxError) {
//                    if (adCloseListener != null)
//                        adCloseListener.onNoAd();
//                    loadInterstitialAds();
//                }
//            });
//
//            loadInterstitialAds();
//        }
//
//        //init rewardAd
//        if (clientConfig.id_reward_ad != null && !"".equals(clientConfig.id_reward_ad)) {
//            rewardedAd = MaxRewardedAd.getInstance(clientConfig.id_reward_ad, currentActivity);
//            rewardedAd.setListener(new MaxRewardedAdListener() {
//                @Override
//                public void onRewardedVideoStarted(MaxAd maxAd) {
//                }
//
//                @Override
//                public void onRewardedVideoCompleted(MaxAd maxAd) {
//
//                }
//
//                @Override
//                public void onUserRewarded(MaxAd maxAd, MaxReward maxReward) {
//                    count_show_reward_completed++;
//                    if (adRewardListener != null)
//                        adRewardListener.onRewarded();
//                }
//
//                @Override
//                public void onAdLoaded(MaxAd maxAd) {
//                    count_load_reward_fail = 0;
//                }
//
//                @Override
//                public void onAdDisplayed(MaxAd maxAd) {
//                    isShowingFullScreenAds = true;
//                    // Called when ad is shown.
//                    lastTimeShowRewardAds = new Date();
//                }
//
//                @Override
//                public void onAdHidden(MaxAd maxAd) {
//                    isShowingFullScreenAds = false;
//                    loadRewardVideoAds();
//                }
//
//                @Override
//                public void onAdClicked(MaxAd maxAd) {
//
//                }
//
//                @Override
//                public void onAdLoadFailed(String s, MaxError maxError) {
//                    count_load_reward_fail++;
//                }
//
//                @Override
//                public void onAdDisplayFailed(MaxAd maxAd, MaxError maxError) {
//                    if (adRewardListener != null)
//                        adRewardListener.onRewarded();
//                    loadRewardVideoAds();
//                }
//            });
//            loadRewardVideoAds();
//        }
//    }
//
//    /**
//     * Request an ad
//     */
//
//    private void loadRewardVideoAds() {
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1 || clientConfig.is_noti == 3 || rewardedAd == null || currentActivity == null) {
//            return;
//        }
//
//        if (count_show_reward_completed >= clientConfig.total_reward_admob)
//            return;
//        Utils.logDebug(this.getClass(), "load reward ads");
//        rewardedAd.loadAd();
//    }
//
//    private void loadInterstitialAds() {
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1 || clientConfig.is_noti == 3 || interstitialAd == null || currentActivity == null) {
//            return;
//        }
//        if (count_show_full_completed >= clientConfig.total_full_admob)
//            return;
//
//        Utils.logDebug(this.getClass(), "load Interstitial ads");
//        interstitialAd.loadAd();
//    }
//
//
//    public void showRewardVideoAds(AdRewardListener adRewardListener) {
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1 || rewardedAd == null) {
//            if (adRewardListener != null)
//                adRewardListener.onAdNotAvailable();
//            return;
//        }
//
//        if(rewardedAd.isReady())
//        {
//            this.adRewardListener = adRewardListener;
//            rewardedAd.showAd();
//        }
//        else
//        {
//            if (adRewardListener != null)
//                adRewardListener.onAdNotAvailable();
//
//            loadRewardVideoAds();
//        }
//    }
//
//    public boolean isHaveFullAds() {
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1 || interstitialAd == null)
//            return false;
//
//        long timeBetween = Long.MAX_VALUE;
//        if (lastTimeShowInterstitialAds != null)
//            timeBetween = new Date().getTime() - lastTimeShowInterstitialAds.getTime();
//        if (timeBetween > clientConfig.delay_show_full * 1000) {
//            if (interstitialAd != null && interstitialAd.isReady())
//                return true;
//        }
//        return false;
//    }
//
//    public boolean isHaveFullAdsWatchVideo() {
//        if (isHaveFullAds() && clientConfig.is_ads_watch_video) {
//            return true;
//        }
//        return false;
//    }
//
//    public boolean isHaveRewardAds() {
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1 || rewardedAd == null)
//            return false;
//
//        long timeBetween = Long.MAX_VALUE;
//        if (lastTimeShowRewardAds != null)
//            timeBetween = new Date().getTime() - lastTimeShowRewardAds.getTime();
//        if (clientConfig.delay_show_reward == 0)
//            clientConfig.delay_show_reward = clientConfig.delay_show_full;
//
//        if (timeBetween > clientConfig.delay_show_reward * 1000) {
//            if (rewardedAd != null && rewardedAd.isReady())
//                return true;
//        }
//        return false;
//    }
//
//    public void showInterstitialAds(Activity currentActivity, AdCloseListener adCloseListener) {
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1 || interstitialAd == null) {
//            if (adCloseListener != null)
//                adCloseListener.onNoAd();
//            return;
//        }
//
//        long timeBetween = Long.MAX_VALUE;
//        if (lastTimeShowInterstitialAds != null)
//            timeBetween = new Date().getTime() - lastTimeShowInterstitialAds.getTime();
//
//        if (timeBetween > clientConfig.delay_show_full * 1000) {
//            if (interstitialAd.isReady()) {
//                showAdmobFull(adCloseListener);
//            } else {
//                loadInterstitialAds();
//                if (adCloseListener != null)
//                    adCloseListener.onNoAd();
//            }
//
//        } else {
//            if (adCloseListener != null)
//                adCloseListener.onNoAd();
//        }
//    }
//
//    public void showInterstitialAds(AdCloseListener adCloseListener) {
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1 || interstitialAd == null) {
//            if (adCloseListener != null)
//                adCloseListener.onNoAd();
//            return;
//        }
//
//        long timeBetween = Long.MAX_VALUE;
//        if (lastTimeShowInterstitialAds != null)
//            timeBetween = new Date().getTime() - lastTimeShowInterstitialAds.getTime();
//
//        if (timeBetween > clientConfig.delay_show_full * 1000) {
//            if (interstitialAd.isReady()) {
//                if (currentActivity != null)
//                    showAdmobFull(adCloseListener);
//                else
//                    adCloseListener.onNoAd();
//            } else {
//                loadInterstitialAds();
//                if (adCloseListener != null)
//                    adCloseListener.onNoAd();
//            }
//
//        } else {
//            if (adCloseListener != null)
//                adCloseListener.onNoAd();
//        }
//    }
//
//    private void showAdmobFull(AdCloseListener adCloseListener) {
////        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
////            @Override
////            public void onAdDismissedFullScreenContent() {
////                // Called when fullscreen content is dismissed.
////                isShowingFullScreenAds = false;
////                if (adCloseListener != null)
////                    adCloseListener.onAdClose();
////                loadInterstitialAds();
////            }
////
////            @Override
////            public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
////                if (adCloseListener != null)
////                    adCloseListener.onNoAd();
////                // Called when fullscreen content failed to show.
////            }
////
////            @Override
////            public void onAdShowedFullScreenContent() {
////                // Called when fullscreen content is shown.
////                // Make sure to set your reference to null so you don't
////                // show it a second time.
////                isShowingFullScreenAds = true;
////                interstitialAd = null;
////                lastTimeShowInterstitialAds = new Date();
////            }
////        });
//        this.adCloseListener = adCloseListener;
//        interstitialAd.showAd();
//    }
//
//    /**
//     * Shows the ad if one isn't already showing.
//     */
////    public void showOpenAdIfAvailable() {
////        // Only show ad if there is not already an app open ad currently showing
////        // and an ad is available.
////        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1 || isShowingFullScreenAds || currentActivity == null) {
////            Utils.logDebug(this.getClass(), "Can not show ad");
////            return;
////        }
////
////        if (!canShowOpen())
////            return;
////
////        long timeBetween = Long.MAX_VALUE;
////        if (lastTimeShowOpenAds != null)
////            timeBetween = new Date().getTime() - lastTimeShowOpenAds.getTime();
////        if (clientConfig.delay_show_openads == 0)
////            clientConfig.delay_show_openads = clientConfig.delay_show_reward;
////        if (timeBetween > clientConfig.delay_show_openads * 1000 && isOpenAdAvailable()) {
////            if (currentActivity instanceof AdOverlayActivity) {
////                Utils.logDebug(this.getClass(), "Will show open ad");
////                AdOverlayActivity activity = (AdOverlayActivity) currentActivity;
////                if (!activity.isShowOpenAds())
////                    return;
////                activity.showLoadingOpenAds((activityVisiable) ->
////                {
////                    if (activityVisiable) {
////                        FullScreenContentCallback fullScreenContentCallback =
////                                new FullScreenContentCallback() {
////                                    @Override
////                                    public void onAdDismissedFullScreenContent() {
////                                        // Set the reference to null so isAdAvailable() returns false.
////                                        AdsManager.this.appOpenAd = null;
////                                        isShowingFullScreenAds = false;
////                                        loadOpenAds();
////                                    }
////
////                                    @Override
////                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
////                                    }
////
////                                    @Override
////                                    public void onAdShowedFullScreenContent() {
////                                        isShowingFullScreenAds = true;
////                                        lastTimeShowOpenAds = new Date();
////                                    }
////                                };
////
////                        appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
////                        appOpenAd.show(currentActivity);
////                    }
////                });
////            }
////        }
////    }
////
////    private boolean canShowOpen() {
////        if (currentActivity == null)
////            return false;
////        SharedPreferences mPrefs = currentActivity.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
////        boolean isNotShowOpen = mPrefs.getBoolean("notShowOpen", false);
////        if (isNotShowOpen) {
////            Utils.logDebug(this.getClass(), "Can not show ad --- notShowopen = " + isNotShowOpen);
////            mPrefs.edit().remove("notShowOpen").apply();
////            return false;
////        } else
////            return true;
////    }
////
//    public void setDoNotShowOpen() {
////        if (currentActivity != null) {
////            SharedPreferences mPrefs = currentActivity.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
////            mPrefs.edit().putBoolean("notShowOpen", true).apply();
////        }
//    }
//    @Override
//    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
//        currentActivity = activity;
//    }
//
//    @Override
//    public void onActivityStarted(@NonNull Activity activity) {
//        currentActivity = activity;
//        Utils.logDebug(this.getClass(), "onActivityStarted " + activity.getLocalClassName());
//    }
//
//    @Override
//    public void onActivityResumed(@NonNull Activity activity) {
//        currentActivity = activity;
//        Utils.logDebug(this.getClass(), "onActivityResumed " + activity.getLocalClassName());
//    }
//
//    @Override
//    public void onActivityPaused(@NonNull Activity activity) {
//        Utils.logDebug(this.getClass(), "onActivityPaused " + activity.getLocalClassName());
//    }
//
//    @Override
//    public void onActivityStopped(@NonNull Activity activity) {
//        Utils.logDebug(this.getClass(), "onActivityStopped " + activity.getLocalClassName());
//    }
//
//    @Override
//    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
//        Utils.logDebug(this.getClass(), "onActivitySaveInstanceState " + activity.getLocalClassName());
//
//    }
//
//    @Override
//    public void onActivityDestroyed(@NonNull Activity activity) {
//        Utils.logDebug(this.getClass(), "onActivityDestroyed " + activity.getLocalClassName());
//    }
//
//    @OnLifecycleEvent(ON_START)
//    public void onStart() {
////        showOpenAdIfAvailable();
//        Utils.logDebug(this.getClass(), "onStart");
//    }
//
////    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
////        long dateDifference = (new Date()).getTime() - this.loadTimeOpenAds;
////        long numMilliSecondsPerHour = 3600000;
////        return (dateDifference < (numMilliSecondsPerHour * numHours));
////    }
//}