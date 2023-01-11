package com.muicvtools.mutils;

public interface GetConfiglistener {
    void onGetConfigOnline(com.muicvtools.mutils.ClientConfig clientConfig);
    //gọi khi không get đc config
    void onGetConfigFail();
    //show loading dialog
//    void onStartLoadOnline();
//    void onGetConfigOffline(com.muicv.mutils.ClientConfig clientConfig);
//    void onNewNotices();
//    void onAppNolongerSupport();
//    void onHasAds();
//    void onUpdateAlert(boolean force);//forece = true bắt buộc update, false tùy chọn
}
