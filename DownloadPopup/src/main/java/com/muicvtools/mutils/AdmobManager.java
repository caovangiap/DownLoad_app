package com.muicvtools.mutils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.Date;

public class AdmobManager {
    //public variable
    public static boolean DEBUG = false;
    public static String PREFERENCE_NAME = "DEFAULT_NAME";
    public static String APPLICATION_ID = "APPLICATION_ID";
    private static AdmobManager sharedInstance;
    private static ClientConfig clientConfig;
    private static boolean isShowingFullScreenAds = false;
    private AppOpenAd appOpenAd = null;
    private InterstitialAd interstitialAd;
    private RewardedAd mRewardedAd;
    private Date lastTimeShowInterstitialAds;
    private Date lastTimeShowRewardAds;
    private Date lastTimeShowOpenAds;
    private int count_load_full_admob_fail = 0;
    private int count_load_reward_admob_fail = 0;
    private int count_load_openads_admob_fail = 0;
    //    private int count_load_reward_admob_completed = 0;
//    private int count_load_full_admob_completed = 0;
//    private int count_load_openads_admob_completed = 0;
    private Activity currentActivity;
    private long loadTimeOpenAds = 0;

    public AdmobManager(Application myApplication) {
//        this.myApplication = myApplication;
//        this.myApplication.registerActivityLifecycleCallbacks(this);
//        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        sharedInstance = this;

//        if (clientConfig == null) {
//            SharedPreferences mPrefs = myApplication.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
//            clientConfig = ApiManager.getSharedInstance().loadOffineConfig(mPrefs);
//            if (mPrefs.contains(PurchaseUtils.purchase_premium) || mPrefs.contains(PurchaseUtils.purchase_remove_ads) || mPrefs.getInt("no_ads", 0) == 1 || mPrefs.getInt("full_version", 0) == 1)
//                clientConfig.is_premium = 1;
//        }

        Utils.logDebug(this.getClass(), "AdmobManager init application");
    }

    public static AdmobManager getInstance() {
        return sharedInstance;
    }

//    public ClientConfig getClient() {
//        return clientConfig;
//    }

//    public void fakeClientConfig(ClientConfig client) {
//        clientConfig = client;
//    }

    public void loadAds(Activity context, ClientConfig client) {
        clientConfig = client;
        currentActivity = context;

        SharedPreferences mPrefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        if (mPrefs.contains(PurchaseUtils.purchase_premium) || mPrefs.contains(PurchaseUtils.purchase_remove_ads) || mPrefs.getInt("no_ads", 0) == 1 || mPrefs.getInt("full_version", 0) == 1)
            clientConfig.data.is_premium = true;

        Utils.logDebug(this.getClass(), "loadAds Debug = " + DEBUG);
        if (clientConfig == null || clientConfig.data.is_premium) {
            return;
        }
        resetNumberAds();
        MobileAds.initialize(context, initializationStatus -> {
            Utils.logDebug(this.getClass(), "Admob MobileAds is successfully initialized.");
            startPreloadAds();
        });
    }

    private void resetNumberAds() {
        count_load_full_admob_fail = 0;
        count_load_openads_admob_fail = 0;
        count_load_reward_admob_fail = 0;
    }

    private void startPreloadAds() {
        loadInterstitialAds();
        loadRewardVideoAds();
        loadOpenAds();
    }

    /**
     * Request an ad
     */
    public void loadOpenAds() {
        if (clientConfig == null || clientConfig.data.is_premium || isOpenAdAvailable()) {
            return;
        }

        if (count_load_openads_admob_fail >= 2 || clientConfig.data.id_open_ad == null || "".equals(clientConfig.data.id_open_ad))
            return;

        Utils.logDebug(this.getClass(), "load Open ads");
        AdRequest request = getAdRequest();
        AppOpenAd.load(
                currentActivity, clientConfig.data.id_open_ad, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                        AdmobManager.this.appOpenAd = appOpenAd;
                        AdmobManager.this.loadTimeOpenAds = (new Date()).getTime();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        count_load_openads_admob_fail++;
                    }
                });
    }

    private void loadRewardVideoAds() {
        if (clientConfig == null || clientConfig.data.is_premium || mRewardedAd != null || currentActivity == null) {
            return;
        }

        if (count_load_reward_admob_fail >= 2 || clientConfig.data.id_reward_ad == null || "".equals(clientConfig.data.id_reward_ad))
            return;
        Utils.logDebug(this.getClass(), "load reward ads");

        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(currentActivity, clientConfig.data.id_reward_ad,
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        mRewardedAd = null;
                        count_load_reward_admob_fail++;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                    }
                });
    }

    private void loadInterstitialAds() {
        if (clientConfig == null || clientConfig.data.is_premium || interstitialAd != null || currentActivity == null) {
            return;
        }

        if (count_load_full_admob_fail > 2 || clientConfig.data.id_full_ad == null || "".equals(clientConfig.data.id_full_ad))
            return;

        if (count_load_full_admob_fail < 2) {
            Utils.logDebug(this.getClass(), "load Interstitial ads");
            AdRequest adRequest = new AdRequest.Builder().build();
            InterstitialAd.load(currentActivity, clientConfig.data.id_full_ad, adRequest, new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd minterstitialAd) {
                    // The mInterstitialAd reference will be null until
                    // an ad is loaded.
                    interstitialAd = minterstitialAd;
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    // Handle the error
                    interstitialAd = null;
                    count_load_full_admob_fail++;
                }
            });
        }

    }

    public void showRewardVideoAds(Activity activity, AdRewardListener adRewardListener) {
        if (clientConfig == null || clientConfig.data.is_premium) {
            if (adRewardListener != null)
                adRewardListener.onAdNotAvailable();
            return;
        }

        if (mRewardedAd != null) {
            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdShowedFullScreenContent() {
                    isShowingFullScreenAds = true;
                    // Called when ad is shown.
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    // Called when ad fails to show.
                    if (adRewardListener != null)
                        adRewardListener.onRewarded();
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    isShowingFullScreenAds = false;
                    mRewardedAd = null;
                    lastTimeShowRewardAds = new Date();
                    loadRewardVideoAds();
                }
            });

            mRewardedAd.show(activity, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
                    if (adRewardListener != null)
                        adRewardListener.onRewarded();
                }
            });
        } else {
            loadRewardVideoAds();
            if (adRewardListener != null)
                adRewardListener.onAdNotAvailable();
        }
    }

    public boolean isHaveFullAds() {
        if (clientConfig == null || clientConfig.data.is_premium)
            return false;

        long timeBetween = Long.MAX_VALUE;
        if (lastTimeShowInterstitialAds != null)
            timeBetween = new Date().getTime() - lastTimeShowInterstitialAds.getTime();
        if (timeBetween > (long) clientConfig.data.delay_show_full * 1000) {
            if (interstitialAd != null)
                return true;
        }

        loadInterstitialAds();
        return false;
    }

    public boolean isHaveFullAdsWatchVideo() {
        if (isHaveFullAds() && clientConfig.data.is_ads_watch_video) {
            return true;
        }
        return false;
    }

    public boolean isHaveRewardAds() {
        if (clientConfig == null || clientConfig.data.is_premium)
            return false;
//
        long timeBetween = Long.MAX_VALUE;
        if (lastTimeShowRewardAds != null)
            timeBetween = new Date().getTime() - lastTimeShowRewardAds.getTime();
        if (clientConfig.data.delay_show_reward == 0)
            clientConfig.data.delay_show_reward = clientConfig.data.delay_show_full;

        if (timeBetween > (long) clientConfig.data.delay_show_reward * 1000) {
            if (mRewardedAd != null)
                return true;
        }

        loadRewardVideoAds();
        return false;
    }

    public void showInterstitialAds(Activity currentActivity, AdCloseListener adCloseListener) {
        if (clientConfig == null || clientConfig.data.is_premium) {
            if (adCloseListener != null)
                adCloseListener.onNoAd();
            return;
        }

        long timeBetween = Long.MAX_VALUE;
        if (lastTimeShowInterstitialAds != null)
            timeBetween = new Date().getTime() - lastTimeShowInterstitialAds.getTime();

        if (timeBetween > (long) clientConfig.data.delay_show_full * 1000) {
            if (interstitialAd != null) {
                showAdmobFull(currentActivity, adCloseListener);
            } else {
                loadInterstitialAds();
                if (adCloseListener != null)
                    adCloseListener.onNoAd();
            }

        } else {
            if (adCloseListener != null)
                adCloseListener.onNoAd();
        }
    }

    public void showInterstitialAds(AdCloseListener adCloseListener) {
        if (clientConfig == null || clientConfig.data.is_premium) {
            if (adCloseListener != null)
                adCloseListener.onNoAd();
            return;
        }

        long timeBetween = Long.MAX_VALUE;
        if (lastTimeShowInterstitialAds != null)
            timeBetween = new Date().getTime() - lastTimeShowInterstitialAds.getTime();

        if (timeBetween > (long) clientConfig.data.delay_show_full * 1000) {
            if (interstitialAd != null) {
                if (currentActivity != null)
                    showAdmobFull(currentActivity, adCloseListener);
                else
                    adCloseListener.onNoAd();
            } else {
                loadInterstitialAds();
                if (adCloseListener != null)
                    adCloseListener.onNoAd();
            }

        } else {
            if (adCloseListener != null)
                adCloseListener.onNoAd();
        }
    }

    private void showAdmobFull(Activity currentActivity, AdCloseListener adCloseListener) {
        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                // Called when fullscreen content is dismissed.
                isShowingFullScreenAds = false;
                if (adCloseListener != null)
                    adCloseListener.onAdClose();

                lastTimeShowInterstitialAds = new Date();
                loadInterstitialAds();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                if (adCloseListener != null)
                    adCloseListener.onNoAd();
                // Called when fullscreen content failed to show.
            }

            @Override
            public void onAdShowedFullScreenContent() {
                // Called when fullscreen content is shown.
                // Make sure to set your reference to null so you don't
                // show it a second time.
                isShowingFullScreenAds = true;
                interstitialAd = null;
            }
        });
        interstitialAd.show(currentActivity);
    }

    /**
     * Shows the ad if one isn't already showing.
     */
    public void showOpenAds() {
        FullScreenContentCallback fullScreenContentCallback =
                new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Set the reference to null so isAdAvailable() returns false.
                        AdmobManager.this.appOpenAd = null;
                        isShowingFullScreenAds = false;
                        lastTimeShowOpenAds = new Date();
                        loadOpenAds();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        isShowingFullScreenAds = true;
                    }
                };

        appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
        appOpenAd.show(currentActivity);
    }

    public void setDoNotShowOpen() {
        if (currentActivity != null) {
            SharedPreferences mPrefs = currentActivity.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            mPrefs.edit().putBoolean("notShowOpen", true).apply();
        }
    }

    /**
     * Creates and returns ad request.
     */
    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    public boolean isOpenAdAvailable() {
        long timeBetween = Long.MAX_VALUE;
        if (lastTimeShowOpenAds != null)
            timeBetween = new Date().getTime() - lastTimeShowOpenAds.getTime();
        if (clientConfig.data.delay_show_openads == 0)
            clientConfig.data.delay_show_openads = clientConfig.data.delay_show_reward;
        if (timeBetween > clientConfig.data.delay_show_openads * 1000) {
            if (appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4))
                return true;
            else {
                return false;
            }
        } else
            return false;
    }

    public void setCurrentActivity(Activity activity) {
        this.currentActivity = activity;
    }

    public boolean getIsShowingFullScreenAds() {
        return isShowingFullScreenAds;
    }

    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTimeOpenAds;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

//    @Override
//    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
//        currentActivity = activity;
//    }
//
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
//        showOpenAdIfAvailable();
//        Utils.logDebug(this.getClass(), "onStart");
//    }


}


//package com.muicvtools.mutils;
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
//import com.google.android.gms.ads.AdError;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.FullScreenContentCallback;
//import com.google.android.gms.ads.LoadAdError;
//import com.google.android.gms.ads.MobileAds;
//import com.google.android.gms.ads.OnUserEarnedRewardListener;
//import com.google.android.gms.ads.appopen.AppOpenAd;
//import com.google.android.gms.ads.interstitial.InterstitialAd;
//import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
//import com.google.android.gms.ads.rewarded.RewardItem;
//import com.google.android.gms.ads.rewarded.RewardedAd;
//import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
//
//import java.util.Date;
//
//public class AdmobManager implements Application.ActivityLifecycleCallbacks, LifecycleObserver {
//    //public variable
//    public static boolean DEBUG = false;
//    public static String PREFERENCE_NAME = "DEFAULT_NAME";
//    public static String APPLICATION_ID = "APPLICATION_ID";
//    private static AdmobManager sharedInstance;
//    private static ClientConfig clientConfig;
//    private static boolean isShowingFullScreenAds = false;
//    private final Application myApplication;
//    private AppOpenAd appOpenAd = null;
//    private InterstitialAd interstitialAd;
//    private RewardedAd mRewardedAd;
//    private Date lastTimeShowInterstitialAds;
//    private Date lastTimeShowRewardAds;
//    private Date lastTimeShowOpenAds;
//    private int count_load_full_admob_fail = 0;
//    private int count_load_reward_admob_fail = 0;
//    private int count_load_openads_admob_fail = 0;
//    private int count_load_reward_admob_completed = 0;
//    private int count_load_full_admob_completed = 0;
//    private int count_load_openads_admob_completed = 0;
//    private Activity currentActivity;
//    private long loadTimeOpenAds = 0;
//
//    public AdmobManager(Application myApplication) {
//        this.myApplication = myApplication;
//        this.myApplication.registerActivityLifecycleCallbacks(this);
//        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
//        sharedInstance = this;
//
////        if (clientConfig == null) {
////            SharedPreferences mPrefs = myApplication.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
////            clientConfig = ApiManager.getSharedInstance().loadOffineConfig(mPrefs);
////            if (mPrefs.contains(PurchaseUtils.purchase_premium) || mPrefs.contains(PurchaseUtils.purchase_remove_ads) || mPrefs.getInt("no_ads", 0) == 1 || mPrefs.getInt("full_version", 0) == 1)
////                clientConfig.is_premium = 1;
////        }
//
//        Utils.logDebug(this.getClass(), "AdmobManager init application");
//    }
//
//    public static AdmobManager getInstance() {
//        return sharedInstance;
//    }
//
////    public ClientConfig getClient() {
////        return clientConfig;
////    }
//
////    public void fakeClientConfig(ClientConfig client) {
////        clientConfig = client;
////    }
//
//    public void loadAds(Activity context, ClientConfig client) {
//        clientConfig = client;
//        currentActivity = context;
//
//        SharedPreferences mPrefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
//        if (mPrefs.contains(PurchaseUtils.purchase_premium) || mPrefs.contains(PurchaseUtils.purchase_remove_ads) || mPrefs.getInt("no_ads", 0) == 1 || mPrefs.getInt("full_version", 0) == 1)
//            clientConfig.is_premium = 1;
//
//        Utils.logDebug(this.getClass(), "loadAds Debug = " + DEBUG);
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1 || clientConfig.is_noti == 3) {
//            return;
//        }
//        resetNumberAds();
//        MobileAds.initialize(myApplication, initializationStatus -> {
//            Utils.logDebug(this.getClass(), "Admob MobileAds is successfully initialized.");
//            startPreloadAds();
//        });
//    }
//
//    private void resetNumberAds() {
//        count_load_full_admob_fail = 0;
//        count_load_openads_admob_fail = 0;
//        count_load_reward_admob_fail = 0;
//
//        count_load_full_admob_completed = 0;
//        count_load_reward_admob_completed = 0;
//        count_load_openads_admob_completed = 0;
//    }
//
//    private void startPreloadAds() {
//        if (clientConfig.total_full_admob == 0)
//            clientConfig.total_full_admob = 1;
//        if (clientConfig.total_reward_admob == 0)
//            clientConfig.total_reward_admob = 1;
//        if (clientConfig.total_open_ads == 0)
//            clientConfig.total_open_ads = 1;
//
////        if (!"".equals(clientConfig.id_open_ad))
////            AppOpenManager.getInstance().fetchAd(clientConfig.id_open_ad, clientConfig.total_open_ads);
//        loadInterstitialAds();
//        loadRewardVideoAds();
//        loadOpenAds();
//    }
//
//    /**
//     * Request an ad
//     */
//    public void loadOpenAds() {
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1 || clientConfig.is_noti == 3 || isOpenAdAvailable()) {
//            return;
//        }
//
//        if (count_load_openads_admob_fail >= 2 || count_load_openads_admob_completed >= clientConfig.total_open_ads || "".equals(clientConfig.id_open_ad))
//            return;
//
//        Utils.logDebug(this.getClass(), "load Open ads");
//        AdRequest request = getAdRequest();
//        AppOpenAd.load(
//                myApplication, clientConfig.id_open_ad, request,
//                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, new AppOpenAd.AppOpenAdLoadCallback() {
//                    @Override
//                    public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
//                        AdmobManager.this.appOpenAd = appOpenAd;
//                        AdmobManager.this.loadTimeOpenAds = (new Date()).getTime();
//                    }
//
//                    @Override
//                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                        count_load_openads_admob_fail++;
//                    }
//                });
//    }
//
//    private void loadRewardVideoAds() {
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1 || clientConfig.is_noti == 3 || mRewardedAd != null || currentActivity == null) {
//            return;
//        }
//
//        if (count_load_reward_admob_fail >= 2 || count_load_reward_admob_completed >= clientConfig.total_reward_admob || "".equals(clientConfig.id_reward_ad))
//            return;
//        Utils.logDebug(this.getClass(), "load reward ads");
//
//        AdRequest adRequest = new AdRequest.Builder().build();
//        RewardedAd.load(currentActivity, clientConfig.id_reward_ad,
//                adRequest, new RewardedAdLoadCallback() {
//                    @Override
//                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                        // Handle the error.
//                        mRewardedAd = null;
//                        count_load_reward_admob_fail++;
//                    }
//
//                    @Override
//                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
//                        mRewardedAd = rewardedAd;
//                        count_load_reward_admob_completed++;
//                    }
//                });
//    }
//
//    private void loadInterstitialAds() {
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1 || clientConfig.is_noti == 3 || interstitialAd != null || currentActivity == null) {
//            return;
//        }
//        if (count_load_full_admob_fail < 2 && count_load_full_admob_completed < clientConfig.total_full_admob) {
//            Utils.logDebug(this.getClass(), "load Interstitial ads");
//            AdRequest adRequest = new AdRequest.Builder().build();
//            InterstitialAd.load(currentActivity, clientConfig.id_full_ad, adRequest, new InterstitialAdLoadCallback() {
//                @Override
//                public void onAdLoaded(@NonNull InterstitialAd minterstitialAd) {
//                    // The mInterstitialAd reference will be null until
//                    // an ad is loaded.
//                    interstitialAd = minterstitialAd;
//                    count_load_full_admob_completed++;
//                }
//
//                @Override
//                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                    // Handle the error
//                    interstitialAd = null;
//                    count_load_full_admob_fail++;
//                }
//            });
//        }
//
//    }
//
//    public void showRewardVideoAds(Activity activity, AdRewardListener adRewardListener) {
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1) {
//            if (adRewardListener != null)
//                adRewardListener.onAdNotAvailable();
//            return;
//        }
//
//        if (mRewardedAd != null) {
//            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//                @Override
//                public void onAdShowedFullScreenContent() {
//                    isShowingFullScreenAds = true;
//                    // Called when ad is shown.
//                    lastTimeShowRewardAds = new Date();
//                }
//
//                @Override
//                public void onAdFailedToShowFullScreenContent(AdError adError) {
//                    // Called when ad fails to show.
//                    if (adRewardListener != null)
//                        adRewardListener.onRewarded();
//                }
//
//                @Override
//                public void onAdDismissedFullScreenContent() {
//                    // Called when ad is dismissed.
//                    // Don't forget to set the ad reference to null so you
//                    // don't show the ad a second time.
//                    isShowingFullScreenAds = false;
//                    mRewardedAd = null;
//                    loadRewardVideoAds();
//                }
//            });
//
//            mRewardedAd.show(activity, new OnUserEarnedRewardListener() {
//                @Override
//                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
//                    // Handle the reward.
//                    if (adRewardListener != null)
//                        adRewardListener.onRewarded();
//                }
//            });
//        } else {
//            loadRewardVideoAds();
//            if (adRewardListener != null)
//                adRewardListener.onAdNotAvailable();
//        }
//    }
//
//    public boolean isHaveFullAds() {
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1)
//            return false;
//
//        long timeBetween = Long.MAX_VALUE;
//        if (lastTimeShowInterstitialAds != null)
//            timeBetween = new Date().getTime() - lastTimeShowInterstitialAds.getTime();
//        if (timeBetween > clientConfig.delay_show_full * 1000) {
//            if (interstitialAd != null)
//                return true;
//        }
//
//        loadInterstitialAds();
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
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1)
//            return false;
////
//        long timeBetween = Long.MAX_VALUE;
//        if (lastTimeShowRewardAds != null)
//            timeBetween = new Date().getTime() - lastTimeShowRewardAds.getTime();
//        if (clientConfig.delay_show_reward == 0)
//            clientConfig.delay_show_reward = clientConfig.delay_show_full;
//
//        if (timeBetween > clientConfig.delay_show_reward * 1000) {
//            if (mRewardedAd != null)
//                return true;
//        }
//
//        loadRewardVideoAds();
//        return false;
//    }
//
//    public void showInterstitialAds(Activity currentActivity, AdCloseListener adCloseListener) {
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1) {
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
//            if (interstitialAd != null) {
//                showAdmobFull(currentActivity, adCloseListener);
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
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1) {
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
//            if (interstitialAd != null) {
//                if (currentActivity != null)
//                    showAdmobFull(currentActivity, adCloseListener);
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
//    private void showAdmobFull(Activity currentActivity, AdCloseListener adCloseListener) {
//        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//            @Override
//            public void onAdDismissedFullScreenContent() {
//                // Called when fullscreen content is dismissed.
//                isShowingFullScreenAds = false;
//                if (adCloseListener != null)
//                    adCloseListener.onAdClose();
//                loadInterstitialAds();
//            }
//
//            @Override
//            public void onAdFailedToShowFullScreenContent(AdError adError) {
//                if (adCloseListener != null)
//                    adCloseListener.onNoAd();
//                // Called when fullscreen content failed to show.
//            }
//
//            @Override
//            public void onAdShowedFullScreenContent() {
//                // Called when fullscreen content is shown.
//                // Make sure to set your reference to null so you don't
//                // show it a second time.
//                isShowingFullScreenAds = true;
//                interstitialAd = null;
//                lastTimeShowInterstitialAds = new Date();
//            }
//        });
//        interstitialAd.show(currentActivity);
//    }
//
//    /**
//     * Shows the ad if one isn't already showing.
//     */
//    private void showOpenAdIfAvailable() {
//        // Only show ad if there is not already an app open ad currently showing
//        // and an ad is available.
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1 || isShowingFullScreenAds || currentActivity == null) {
//            Utils.logDebug(this.getClass(), "Can not show ad");
//            return;
//        }
//
//        if (!canShowOpen())
//            return;
//
//        long timeBetween = Long.MAX_VALUE;
//        if (lastTimeShowOpenAds != null)
//            timeBetween = new Date().getTime() - lastTimeShowOpenAds.getTime();
//        if (clientConfig.delay_show_openads == 0)
//            clientConfig.delay_show_openads = clientConfig.delay_show_reward;
//        if (timeBetween > clientConfig.delay_show_openads * 1000 && isOpenAdAvailable()) {
//            if (currentActivity instanceof AdOverlayActivity) {
//                Utils.logDebug(this.getClass(), "Will show open ad");
//                AdOverlayActivity activity = (AdOverlayActivity) currentActivity;
//                if (!activity.isShowOpenAds())
//                    return;
//                activity.showLoadingOpenAds((activityVisiable) ->
//                {
//                    if (activityVisiable) {
//                        FullScreenContentCallback fullScreenContentCallback =
//                                new FullScreenContentCallback() {
//                                    @Override
//                                    public void onAdDismissedFullScreenContent() {
//                                        // Set the reference to null so isAdAvailable() returns false.
//                                        AdmobManager.this.appOpenAd = null;
//                                        isShowingFullScreenAds = false;
//                                        loadOpenAds();
//                                    }
//
//                                    @Override
//                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
//                                    }
//
//                                    @Override
//                                    public void onAdShowedFullScreenContent() {
//                                        isShowingFullScreenAds = true;
//                                        lastTimeShowOpenAds = new Date();
//                                    }
//                                };
//
//                        appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
//                        appOpenAd.show(currentActivity);
//                    }
//                });
//            }
//        }
//    }
//
//    private boolean canShowOpen() {
//        if (currentActivity == null)
//            return false;
//        SharedPreferences mPrefs = currentActivity.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
//        boolean isNotShowOpen = mPrefs.getBoolean("notShowOpen", false);
//        if (isNotShowOpen) {
//            Utils.logDebug(this.getClass(), "Can not show ad --- notShowopen = " + isNotShowOpen);
//            mPrefs.edit().remove("notShowOpen").apply();
//            return false;
//        } else
//            return true;
//    }
//
//    public void setDoNotShowOpen() {
//        if (currentActivity != null) {
//            SharedPreferences mPrefs = currentActivity.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
//            mPrefs.edit().putBoolean("notShowOpen", true).apply();
//        }
//    }
//
//    /**
//     * Creates and returns ad request.
//     */
//    private AdRequest getAdRequest() {
//        return new AdRequest.Builder().build();
//    }
//
//    /**
//     * Utility method that checks if ad exists and can be shown.
//     */
//    public boolean isOpenAdAvailable() {
//        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
//    }
//
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
//        showOpenAdIfAvailable();
//        Utils.logDebug(this.getClass(), "onStart");
//    }
//
//    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
//        long dateDifference = (new Date()).getTime() - this.loadTimeOpenAds;
//        long numMilliSecondsPerHour = 3600000;
//        return (dateDifference < (numMilliSecondsPerHour * numHours));
//    }
//}