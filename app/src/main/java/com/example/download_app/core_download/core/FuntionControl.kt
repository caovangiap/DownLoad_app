package com.example.download_app.core_download.core

import java.io.File

interface  FuntionControl {
<<<<<<< HEAD
    fun onStart()
=======
    fun onStartDownload()
>>>>>>> origin/master
    fun onPause()
    fun onResume()
    fun onProgressUpdate(percent: Int, downloadedSize: Int, totalSize: Int)
    fun onCompleted(file: File?)
    fun onFailure(reason: String?)
    fun onCancel()
}