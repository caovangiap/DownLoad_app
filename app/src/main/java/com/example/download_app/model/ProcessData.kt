package com.example.download_app.model

import android.os.Parcelable
import androidx.lifecycle.LiveData
import kotlinx.parcelize.Parcelize

@Parcelize

data class ProcessData(
    val id_ProcessDownLoad: Long,
    var textPercent: String,
    var intPercent : Int,
    val uri: String,
    // điều kiện tiếp tục hay dừng cập nhật tiến trình download
    var conditionDownLoad: String,
    val fileName: String,
    var status : String,
    var totalSizeFile : String,
    var conditionControl : String
) : Parcelable