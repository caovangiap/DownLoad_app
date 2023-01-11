package com.muicvtools.mutils;//package com.muicv.mutils;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.SharedPreferences;
//
//import androidx.annotation.NonNull;
//
//import com.muicv.mutils.Constants;
//import com.muicv.mutils.MainActivity;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.FullScreenContentCallback;
//import com.google.android.gms.ads.LoadAdError;
//import com.google.android.gms.ads.MobileAds;
//import com.google.android.gms.ads.OnUserEarnedRewardListener;
//import com.google.android.gms.ads.interstitial.InterstitialAd;
//import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
//import com.google.android.gms.ads.rewarded.RewardItem;
//import com.google.android.gms.ads.rewarded.RewardedAd;
//import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
//
//import java.util.Date;
//
//public class InterstitialUtils {
//
//    private static InterstitialUtils sharedInstance;
//
//    private InterstitialAd interstitialAd;
//    private AdCloseListener adCloseListener;
//    private AdRewardListener adRewardListener;
//
//    private Date lastTimeShowInterstitialAds;
//    private Date lastTimeShowRewardAds;
//    private ClientConfig clientConfig;
//
//    private RewardedAd mRewardedAd;
//    private int count_load_admob_fail = 0;
//    private int count_load_reward_admob_fail = 0;
//    private int count_load_reward_admob_completed = 0;
//    private int count_load_full_admob_completed = 0;
//
//    private Activity currentContext;
//    public static boolean isShowingFullScreenAds = false;
//
//    public static InterstitialUtils getSharedInstance() {
//        if (sharedInstance == null) {
//            sharedInstance = new InterstitialUtils();
//        }
//        return sharedInstance;
//    }
//
//    public ClientConfig getClient() {
//        return clientConfig;
//    }
//
//    public void init(Activity context, ClientConfig client) {
//        clientConfig = client;
//        SharedPreferences mPref = context.getSharedPreferences(MainActivity.PREFEREN_NAME, Context.MODE_PRIVATE);
//        if(mPref.contains(Constants.purchase_premium) || mPref.contains(Constants.purchase_remove_ads) || mPref.getInt("no_ads",0) == 1 || mPref.getInt("full_version",0) == 1  )
//            clientConfig.is_premium = 1;
//
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1) {
//            return;
//        }
//
//        if(!"".equals(clientConfig.id_full_ad)  || !"".equals(clientConfig.id_reward_ad))
//        {
//            currentContext = context;
//            MobileAds.initialize(context,initializationStatus -> {
//                startPreloadAds();
//            });
//        }
//    }
//
//    private void startPreloadAds()
//    {
//        if (clientConfig.total_full_admob == 0)
//            clientConfig.total_full_admob = 1;
//        if (clientConfig.total_reward_admob == 0)
//            clientConfig.total_reward_admob = 1;
//        if (clientConfig.total_open_ads == 0)
//            clientConfig.total_open_ads = 1;
//
//        if (!"".equals(clientConfig.id_open_ad))
//            AppOpenManager.getInstance().fetchAd(clientConfig.id_open_ad, clientConfig.total_open_ads);
//        loadInterstitialAds();
//        loadRewardVideoAds();
//    }
//
//    private void loadRewardVideoAds() {
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1) {
//            return;
//        }
//
//        if (count_load_reward_admob_fail >= 1 || count_load_reward_admob_completed >= clientConfig.total_reward_admob || "".equals(clientConfig.id_reward_ad))
//            return;
//        AdRequest adRequest = new AdRequest.Builder().build();
//        RewardedAd.load(currentContext, clientConfig.id_reward_ad,
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
//        if (clientConfig == null || clientConfig.is_accept == 0 || clientConfig.is_premium == 1) {
//            return;
//        }
//        if (count_load_admob_fail < 1 && count_load_full_admob_completed < clientConfig.total_full_admob)
//        {
//            AdRequest adRequest = new AdRequest.Builder().build();
//            InterstitialAd.load(currentContext, clientConfig.id_full_ad, adRequest, new InterstitialAdLoadCallback() {
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
//                    count_load_admob_fail++;
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
//            this.adRewardListener = adRewardListener;
//            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//                @Override
//                public void onAdShowedFullScreenContent() {
//                    isShowingFullScreenAds = true;
//                    // Called when ad is shown.
//                    lastTimeShowRewardAds = new Date();
//                }
//
//                @Override
//                public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
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
//    private void showAdmobFull(Activity currentActivity, AdCloseListener adCloseListener) {
//        this.adCloseListener = adCloseListener;
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
//            public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
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
//}