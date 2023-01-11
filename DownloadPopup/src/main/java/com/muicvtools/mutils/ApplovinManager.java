package com.muicvtools.mutils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxAppOpenAd;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;

import java.util.Date;

public class ApplovinManager {
    private static ApplovinManager sharedInstance;

    private static ClientConfig clientConfig;

    private MaxInterstitialAd interstitialAd;
    private MaxRewardedAd rewardedAd;
    private static boolean isShowingFullScreenAds = false;

    private Date lastTimeShowInterstitialAds;
    private Date lastTimeShowRewardAds;
    private MaxAppOpenAd appOpenAd;

    private AdRewardListener adRewardListener;
    private AdCloseListener adCloseListener;
    private Date lastTimeShowOpenAds;
    private Activity currentActivity;

    //public variable
    public static boolean DEBUG = false;
    public static String PREFERENCE_NAME = "DEFAULT_NAME";
    public static String APPLICATION_ID = "APPLICATION_ID";

    public ApplovinManager(Application myApplication) {
        sharedInstance = this;

        if (clientConfig == null) {
            SharedPreferences mPrefs = myApplication.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            clientConfig = ApiManager.getSharedInstance().loadOffineConfig(mPrefs);
            if (mPrefs.contains(PurchaseUtils.purchase_premium) || mPrefs.contains(PurchaseUtils.purchase_remove_ads) || mPrefs.getInt("no_ads", 0) == 1 || mPrefs.getInt("full_version", 0) == 1)
                clientConfig.data.is_premium = true;
        }

        Utils.logDebug(this.getClass(), "ApplovinManager init application");
    }

    public static ApplovinManager getInstance() {
        return sharedInstance;
    }

//    public ClientConfig getClient() {
//        return clientConfig;
//    }
//
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
        if (clientConfig == null || clientConfig.data.is_premium == true) {
            return;
        }
        if (clientConfig.data.applovin_sdk_key != null && !"".equals(clientConfig.data.applovin_sdk_key)) {
            resetNumberAds();
            try {
                ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                applicationInfo.metaData.putString("applovin.sdk.key",
                        client.data.applovin_sdk_key);

                AppLovinSdk.getInstance(context).setMediationProvider("max");
                AppLovinSdk.initializeSdk(context, new AppLovinSdk.SdkInitializationListener() {
                    @Override
                    public void onSdkInitialized(final AppLovinSdkConfiguration configuration) {
                        Utils.logDebug(ApplovinManager.class, "Mediation is successfully initialized.");
                        startPreloadAds();
                    }
                });
            } catch (Exception e) {


            }

        }
    }

    private void resetNumberAds() {
//        count_show_full_completed = 0;
//        count_show_reward_completed = 0;
    }

    private void startPreloadAds() {
        if (clientConfig == null || clientConfig.data.is_premium == true || currentActivity == null) {
            return;
        }

        interstitialAd = null;
        rewardedAd = null;
        //init interstitial
        if (clientConfig.data.id_full_med != null && !"".equals(clientConfig.data.id_full_med)) {
            interstitialAd = new MaxInterstitialAd(clientConfig.data.id_full_med, currentActivity);
            interstitialAd.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(MaxAd maxAd) {
                }

                @Override
                public void onAdDisplayed(MaxAd maxAd) {
                    isShowingFullScreenAds = true;
                }

                @Override
                public void onAdHidden(MaxAd maxAd) {
                    isShowingFullScreenAds = false;
                    if (adCloseListener != null)
                        adCloseListener.onAdClose();
                    lastTimeShowInterstitialAds = new Date();
                    loadInterstitialAds();
                }

                @Override
                public void onAdClicked(MaxAd maxAd) {

                }

                @Override
                public void onAdLoadFailed(String s, MaxError maxError) {

                }

                @Override
                public void onAdDisplayFailed(MaxAd maxAd, MaxError maxError) {
                    if (adCloseListener != null)
                        adCloseListener.onNoAd();
                    loadInterstitialAds();
                }
            });

            loadInterstitialAds();
        }

        //init rewardAd
        if (clientConfig.data.id_reward_med != null && !"".equals(clientConfig.data.id_reward_med)) {
            rewardedAd = MaxRewardedAd.getInstance(clientConfig.data.id_reward_med, currentActivity);
            rewardedAd.setListener(new MaxRewardedAdListener() {
                @Override
                public void onRewardedVideoStarted(MaxAd maxAd) {
                }

                @Override
                public void onRewardedVideoCompleted(MaxAd maxAd) {

                }

                @Override
                public void onUserRewarded(MaxAd maxAd, MaxReward maxReward) {
                    if (adRewardListener != null)
                        adRewardListener.onRewarded();
                }

                @Override
                public void onAdLoaded(MaxAd maxAd) {
                }

                @Override
                public void onAdDisplayed(MaxAd maxAd) {
                    isShowingFullScreenAds = true;
                    // Called when ad is shown.
                }

                @Override
                public void onAdHidden(MaxAd maxAd) {
                    isShowingFullScreenAds = false;
                    lastTimeShowRewardAds = new Date();
                    loadRewardVideoAds();
                }

                @Override
                public void onAdClicked(MaxAd maxAd) {

                }

                @Override
                public void onAdLoadFailed(String s, MaxError maxError) {
                }

                @Override
                public void onAdDisplayFailed(MaxAd maxAd, MaxError maxError) {
                    if (adRewardListener != null)
                        adRewardListener.onRewarded();
                    loadRewardVideoAds();
                }
            });
            loadRewardVideoAds();
        }

        //init app open
        if (clientConfig.data.id_open_med != null && !"".equals(clientConfig.data.id_open_med)) {
            appOpenAd = new MaxAppOpenAd(clientConfig.data.id_open_med, currentActivity);
            appOpenAd.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(MaxAd ad) {
                }

                @Override
                public void onAdDisplayed(MaxAd ad) {
                    isShowingFullScreenAds = true;
                }

                @Override
                public void onAdHidden(MaxAd ad) {
                    isShowingFullScreenAds = false;
                    lastTimeShowOpenAds = new Date();
                    loadOpenAds();
                }

                @Override
                public void onAdClicked(MaxAd ad) {

                }

                @Override
                public void onAdLoadFailed(String adUnitId, MaxError error) {

                }

                @Override
                public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                    loadOpenAds();
                }
            });
            loadOpenAds();
        }
    }

    /**
     * Request an ad
     */

    private void loadRewardVideoAds() {
        if (clientConfig == null || clientConfig.data.is_premium == true || rewardedAd == null || currentActivity == null) {
            return;
        }

        Utils.logDebug(this.getClass(), "load reward ads");
        rewardedAd.loadAd();
    }

    private void loadInterstitialAds() {
        if (clientConfig == null || clientConfig.data.is_premium == true || interstitialAd == null || currentActivity == null) {
            return;
        }

        Utils.logDebug(this.getClass(), "load Interstitial ads");
        interstitialAd.loadAd();
    }

    private void loadOpenAds() {
        if (clientConfig == null || clientConfig.data.is_premium == true || appOpenAd == null || currentActivity == null) {
            return;
        }

        Utils.logDebug(this.getClass(), "load Open Med ads");
        appOpenAd.loadAd();
    }

    public void showRewardVideoAds(AdRewardListener adRewardListener) {
        if (clientConfig == null || clientConfig.data.is_premium == true || rewardedAd == null) {
            if (adRewardListener != null)
                adRewardListener.onAdNotAvailable();
            return;
        }

        if (rewardedAd.isReady()) {
            this.adRewardListener = adRewardListener;
            rewardedAd.showAd();
        } else {
            if (adRewardListener != null)
                adRewardListener.onAdNotAvailable();

            loadRewardVideoAds();
        }
    }

    public boolean isHaveFullAds() {
        if (clientConfig == null || clientConfig.data.is_premium == true || interstitialAd == null)
            return false;

        long timeBetween = Long.MAX_VALUE;
        if (lastTimeShowInterstitialAds != null)
            timeBetween = new Date().getTime() - lastTimeShowInterstitialAds.getTime();
        if (timeBetween > clientConfig.data.delay_show_full * 1000) {
            if (interstitialAd != null && interstitialAd.isReady())
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
        if (clientConfig == null || clientConfig.data.is_premium || rewardedAd == null)
            return false;

        long timeBetween = Long.MAX_VALUE;
        if (lastTimeShowRewardAds != null)
            timeBetween = new Date().getTime() - lastTimeShowRewardAds.getTime();
        if (clientConfig.data.delay_show_reward == 0)
            clientConfig.data.delay_show_reward = clientConfig.data.delay_show_full;

        if (timeBetween > clientConfig.data.delay_show_reward * 1000) {
            if (rewardedAd != null && rewardedAd.isReady())
                return true;
        }

        loadRewardVideoAds();
        return false;
    }

    public void showInterstitialAds(Activity currentActivity, AdCloseListener adCloseListener) {
        if (clientConfig == null || !clientConfig.data.is_accept || clientConfig.data.is_premium || interstitialAd == null) {
            if (adCloseListener != null)
                adCloseListener.onNoAd();
            return;
        }

        long timeBetween = Long.MAX_VALUE;
        if (lastTimeShowInterstitialAds != null)
            timeBetween = new Date().getTime() - lastTimeShowInterstitialAds.getTime();

        if (timeBetween > clientConfig.data.delay_show_full * 1000) {
            if (interstitialAd.isReady()) {
                showAdmobFull(adCloseListener);
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
        if (clientConfig == null || clientConfig.data.is_premium == true || interstitialAd == null) {
            if (adCloseListener != null)
                adCloseListener.onNoAd();
            return;
        }

        long timeBetween = Long.MAX_VALUE;
        if (lastTimeShowInterstitialAds != null)
            timeBetween = new Date().getTime() - lastTimeShowInterstitialAds.getTime();

        if (timeBetween > clientConfig.data.delay_show_full * 1000) {
            if (interstitialAd.isReady()) {
                if (currentActivity != null)
                    showAdmobFull(adCloseListener);
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

    private void showAdmobFull(AdCloseListener adCloseListener) {
        this.adCloseListener = adCloseListener;
        interstitialAd.showAd();
    }

    public void setCurrentActivity(Activity activity) {
        this.currentActivity = activity;
    }

    public void showOpenAds() {
        if (appOpenAd != null)
            appOpenAd.showAd();
    }

    public boolean isOpenAdAvailable() {
        long timeBetween = Long.MAX_VALUE;
        if (lastTimeShowOpenAds != null)
            timeBetween = new Date().getTime() - lastTimeShowOpenAds.getTime();
        if (clientConfig.data.delay_show_openads == 0)
            clientConfig.data.delay_show_openads = clientConfig.data.delay_show_reward;
        if (timeBetween > clientConfig.data.delay_show_openads * 1000) {
            if (appOpenAd == null || !AppLovinSdk.getInstance(currentActivity).isInitialized())
                return false;
            if (appOpenAd.isReady()) {
                return true;
            } else {
                return false;
            }
        } else
            return false;
    }

    public boolean getIsShowingFullScreenAds() {
        return isShowingFullScreenAds;
    }
    /**
     * Shows the ad if one isn't already showing.
     */
}