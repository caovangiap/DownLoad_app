package com.muicvtools.mutils;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import us.shandian.giga.settings.NewPipeSettings;

public abstract class MainLoadingActivity extends DownloadYtActivity {
    protected abstract void onNewNotices();

    protected abstract void onHasAds();

    protected abstract void onClientConfigLoaded();

    protected abstract void onUpdateAlert(boolean force);

    protected abstract void onFakeClientConfig(ClientConfig clientConfig);

    private boolean isMustShowErrorPopup = false;
    private boolean isInitLoadingConfig = false;
    private boolean isShowLoadingFirstRun = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private boolean checkStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Build.VERSION.SDK_INT > Build.VERSION_CODES.Q)
            return true;

        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return false;
        }
    }

    private void exitAppNow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        } else {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getConfigOnline();
            } else {
                Toast.makeText(this, R.string.permission_not_granted, Toast.LENGTH_SHORT).show();
                exitAppNow();
            }
        }
    }

    protected void initConfigApp() {
        NewPipeSettings.initSettings(this);
        PurchaseUtils.getSharedInstance().init(this);

        if (checkStoragePermissionGranted())
            getConfigOnline();
    }

    private void getConfigOnline() {
        if (AdsManager.getInstance() == null || AdsManager.getInstance().getClient() == null) {
            //get Online
            isShowLoadingFirstRun = true;
            showLoadingPopup("");
        } else {
            ClientConfig client = AdsManager.getInstance().getClient();
            if (AdsManager.DEBUG)
                onFakeClientConfig(client);
            AdsManager.getInstance().fakeClientConfig(client);
            initSetting();
        }

        ApiManager.getSharedInstance().getConfig(this, new GetConfiglistener() {
            @Override
            public void onGetConfigOnline(ClientConfig clientConfig) {
                runOnUiThread(() ->
                {
                    if (AdsManager.DEBUG)
                        onFakeClientConfig(clientConfig);
                    AdsManager.getInstance().loadAds(MainLoadingActivity.this, clientConfig);

                    addMediationBannerAds();
                });

                doGetConfigOnline();

            }

            @Override
            public void onGetConfigFail() {
                runOnUiThread(() ->
                {
                    if (isShowLoadingFirstRun) {
                        isShowLoadingFirstRun = false;
                        showErrorConnection();
                    }
                });
            }
        });
    }

    private void doGetConfigOnline() {
        if (activityVisible) {
            runOnUiThread(() ->
            {
                if (isShowLoadingFirstRun) {
                    isShowLoadingFirstRun = false;
                    dismissLoadingPopup();
                    initSetting();
                    Utils.logDebug(this.getClass(), "getConfigApp done!");
                } else
                    Utils.logDebug(this.getClass(), "save ConfigApp done!");
            });
        } else {
            isInitLoadingConfig = true;
        }
    }

    private void initSetting() {
        onClientConfigLoaded();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (isMustShowErrorPopup) {
            isMustShowErrorPopup = false;
            showErrorConnection();
        } else if (isInitLoadingConfig) {
            isInitLoadingConfig = false;
            doGetConfigOnline();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void showErrorConnection() {
        if (activityVisible) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissLoadingPopup();
                    AlertDialog.Builder builder;
                    builder = new AlertDialog.Builder(MainLoadingActivity.this);
                    AlertDialog alertDialog = builder.setTitle(R.string.title_error_connection)
                            .setMessage(R.string.message_error_connection)
                            .setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                    dialog.dismiss();
                                    getConfigOnline();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setCancelable(false)
                            .create();
                    alertDialog.show();
                }
            });
        } else
            isMustShowErrorPopup = true;
    }
}
