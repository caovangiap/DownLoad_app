package com.example.download_app.test_application.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize

data class DownLoadProcess(
    var textPercent: String,
    var intPercent : Int,
    val uri: String,
    var status : String,
    var totalSizeFile : String,
    var textPercentByte : String
) : Parcelable