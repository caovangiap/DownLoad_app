package com.muicvtools.mutils;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.sdk.AppLovinSdkUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

public abstract class AdOverlayActivity extends AppCompatActivity {
    protected abstract boolean isShowOpenAds();

    protected final CustomProgressDialog customProgressDialog = new CustomProgressDialog();
    public boolean activityVisible = false;
    protected SharedPreferences mPrefs;

    private MaxAdView adView;
    private AdView adAdmobView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences(AdsManager.PREFERENCE_NAME, MODE_PRIVATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        activityVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityVisible = false;
    }

    protected ViewGroup getBannerView() {
        return null;
    }

    protected AdViewListener getAdViewListener() {
        return null;
    }

    public void showInterstitialAds(AdCloseListener adCloseListener) {
//        customProgressDialog.show(this, "black", "Show Ads");
//        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                customProgressDialog.dismiss();
//
//            }
//        }, Constants.DELAY_FULL_ADS);

        if (activityVisible)
            AdsManager.getInstance().showInterstitialAds(AdOverlayActivity.this, adCloseListener);
        else if (adCloseListener != null)
            adCloseListener.onNoAd();
    }

    public void showLoadingOpenAds(LoadingPopupListener listener) {
        customProgressDialog.show(this, "black", "Show Ads");
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                customProgressDialog.dismiss();
                if (listener != null)
                    listener.onEndLoading(activityVisible);
            }
        }, Constants.DELAY_OPEN_ADS);
    }


    public void showLoadingPopup(String message) {
        customProgressDialog.show(this, "black", message);
    }

    public void dismissLoadingPopup() {
        customProgressDialog.dismiss();
    }

    public void showPurchaseDialog(boolean isHasRemoveAdsPurchase) {
        final SharedPreferences preferences
                = android.preference.PreferenceManager.getDefaultSharedPreferences(AdOverlayActivity.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert);
        View root = getLayoutInflater().inflate(R.layout.popup_premium, null);
        builder.setTitle(R.string.title_premium_dialog)
                .setView(root)
                .setNegativeButton("CANCEL", (dialogInterface, i) -> {
                })
                .setCancelable(false);
        AlertDialog alertDialog = builder.create();


        Button btn_premium = root.findViewById(R.id.btn_premium);
        Button btn_restore_premium = root.findViewById(R.id.btn_restore_premium);

        btn_restore_premium.setOnClickListener(v ->
                {
                    alertDialog.dismiss();
                    showDialogRestore();
                }
        );

        if (isHasRemoveAdsPurchase) {
            Button btn_remove_ads = root.findViewById(R.id.btn_remove_ads);
            btn_remove_ads.setText(PurchaseUtils.getSharedInstance().getPrice(PurchaseUtils.purchase_remove_ads));
            btn_remove_ads.setOnClickListener(v ->
                    {
                        if (Utils.isPurchasedRemoveAdsVersion(this))
                            Toast.makeText(AdOverlayActivity.this, R.string.message_you_purchased_this_package, Toast.LENGTH_SHORT).show();
                        else
                            PurchaseUtils.getSharedInstance().purchaseRemoveAds(this, new PurchaseListener() {
                                @Override
                                public void purchaseFailed(String item) {
                                    Toast.makeText(AdOverlayActivity.this, R.string.message_purchased_failed, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void purchaseSuccess(String item) {
                                    alertDialog.dismiss();
                                    Toast.makeText(AdOverlayActivity.this, R.string.restart_take_effect, Toast.LENGTH_SHORT).show();
                                    ApiManager.getSharedInstance().purchase(AdOverlayActivity.this, PurchaseUtils.purchase_remove_ads);
                                }

                                @Override
                                public void purchaseCancel(String item) {
                                    Toast.makeText(AdOverlayActivity.this, R.string.message_purchased_canceled, Toast.LENGTH_SHORT).show();
                                }
                            });
                    }
            );
        } else {
            RelativeLayout remove_ads_layout = root.findViewById(R.id.option1);
            remove_ads_layout.setVisibility(View.GONE);
        }


        btn_premium.setText(PurchaseUtils.getSharedInstance().getPrice(PurchaseUtils.purchase_premium));
        btn_premium.setOnClickListener(v ->
                {
                    if (Utils.isPurchasedPremiumVersion(this))
                        Toast.makeText(AdOverlayActivity.this, R.string.message_you_purchased_this_package, Toast.LENGTH_SHORT).show();
                    else
                        PurchaseUtils.getSharedInstance().purchasePremium(this, new PurchaseListener() {
                            @Override
                            public void purchaseFailed(String item) {
                                Toast.makeText(AdOverlayActivity.this, R.string.message_purchased_failed, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void purchaseSuccess(String item) {
                                alertDialog.dismiss();
                                preferences.edit().putBoolean(
                                        AdOverlayActivity.this.getString(R.string.show_higher_resolutions_key), true).apply();
                                Toast.makeText(AdOverlayActivity.this, R.string.restart_take_effect, Toast.LENGTH_SHORT).show();
                                ApiManager.getSharedInstance().purchase(AdOverlayActivity.this, PurchaseUtils.purchase_premium);
                            }

                            @Override
                            public void purchaseCancel(String item) {
                                Toast.makeText(AdOverlayActivity.this, R.string.message_purchased_canceled, Toast.LENGTH_SHORT).show();
                            }
                        });
                }
        );


        alertDialog.show();
    }

    public void showDialogRestore() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert);
        View root = getLayoutInflater().inflate(R.layout.layout_restore, null);
        builder.setTitle(R.string.restore_premium)
                .setView(root)
                .setNegativeButton("CANCEL", (dialogInterface, i) -> {
                })
                .setCancelable(false);
        AlertDialog alertDialog = builder.create();

        Button btn_restore_purchase_license = root.findViewById(R.id.btn_restore_purchase_license);
        Button btn_restore_purchase_store = root.findViewById(R.id.btn_restore_purchase_store);
        btn_restore_purchase_license.setOnClickListener(view ->
        {
            alertDialog.dismiss();
            showLoadingPopup("Restore");
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismissLoadingPopup();
                    ApiManager.getSharedInstance().restorePremiumLicense(AdOverlayActivity.this, new LicenseListener() {
                        @Override
                        public void onLicenseSuccess() {
                            Toast.makeText(AdOverlayActivity.this, R.string.restart_take_effect, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLicenseFail() {
                            Toast.makeText(AdOverlayActivity.this, R.string.message_restore_purchased_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }, Constants.DELAY_FULL_ADS);
        });

        btn_restore_purchase_store.setOnClickListener(view -> {
            PurchaseUtils.getSharedInstance().restorePremium(this, result -> {
                if (result) {
                    runOnUiThread(() -> alertDialog.dismiss());
                    Toast.makeText(AdOverlayActivity.this, R.string.restart_take_effect, Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(AdOverlayActivity.this, R.string.message_restore_purchased_failed, Toast.LENGTH_SHORT).show();
            });
        });

        alertDialog.show();
    }

    protected void addMediationBannerAds() {
        ViewGroup banner = getBannerView();
        if (banner == null)
            return;

        if (AdsManager.getInstance().getTypeOfBanner() == TYPE_BANNER.NONE) {
            if (getAdViewListener() != null)
                getAdViewListener().onNoAds();
            return;
        }

        if (AdsManager.getInstance().getTypeOfBanner() == TYPE_BANNER.ADMOB) {
            adAdmobView = new AdView(this);
            adAdmobView.setAdSize(AdSize.LARGE_BANNER);
            adAdmobView.setAdUnitId(AdsManager.getInstance().getClient().data.id_banner_ad);
            banner.addView(adAdmobView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adAdmobView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    if (getAdViewListener() != null)
                        getAdViewListener().onAdFailed();
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    if (getAdViewListener() != null)
                        getAdViewListener().onAdLoaded();
                }
            });
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int heightPx = AppLovinSdkUtils.dpToPx(this, 100);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, heightPx);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            adAdmobView.setLayoutParams(params);
            adAdmobView.loadAd(adRequest);
        } else if (AdsManager.getInstance().getTypeOfBanner() == TYPE_BANNER.MEDIATION) {
            adView = new MaxAdView(AdsManager.getInstance().getClient().data.id_banner_med, this);
            adView.setListener(new MaxAdViewAdListener() {
                @Override
                public void onAdExpanded(MaxAd maxAd) {

                }

                @Override
                public void onAdCollapsed(MaxAd maxAd) {

                }

                @Override
                public void onAdLoaded(MaxAd maxAd) {
                    if (getAdViewListener() != null)
                        getAdViewListener().onAdLoaded();
                }

                @Override
                public void onAdDisplayed(MaxAd maxAd) {

                }

                @Override
                public void onAdHidden(MaxAd maxAd) {

                }

                @Override
                public void onAdClicked(MaxAd maxAd) {

                }

                @Override
                public void onAdLoadFailed(String s, MaxError maxError) {
                    if (getAdViewListener() != null)
                        getAdViewListener().onAdFailed();
                }

                @Override
                public void onAdDisplayFailed(MaxAd maxAd, MaxError maxError) {

                }
            });
            int width = ViewGroup.LayoutParams.MATCH_PARENT;

            // Get the adaptive banner height.
            int heightDp = MaxAdFormat.BANNER.getAdaptiveSize(this).getHeight();
            int heightPx = AppLovinSdkUtils.dpToPx(this, heightDp);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, heightPx);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);

            banner.addView(adView);
            adView.setLayoutParams(params);
            adView.loadAd();
        }
    }

    public void hideBanner() {
        if (adView != null) {
            adView.setVisibility(View.GONE);
            adView.stopAutoRefresh();
        } else if (adAdmobView != null) {
            adAdmobView.setVisibility(View.INVISIBLE);
        }
    }

    public void showBanner() {
        if (adView != null) {
            adView.setVisibility(View.VISIBLE);
            adView.startAutoRefresh();
        } else if (adAdmobView != null) {
            adAdmobView.setVisibility(View.VISIBLE);
        }
    }
}

interface LoadingPopupListener {
    void onEndLoading(boolean activityVisiable);
}

enum TYPE_BANNER {
    ADMOB,
    MEDIATION,
    NONE
}
