package com.example.download_app

import android.app.Application
import com.muicvtools.mutils.AdsManager

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        AdsManager(this)
        AdsManager.PREFERENCE_NAME = "com.example.download_app"
    }
}