package com.example.download_app.model

import android.net.Uri

data class StorageData(
    var Uri: Uri,
    var Name: String,
    var Dration: String,
    val Size: Int,
    val id: Long
)
