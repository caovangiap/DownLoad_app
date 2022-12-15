package com.example.download_app.test_application.model

import android.net.Uri

data class StorageData(
    var Uri: Uri,
    var Name: String,
    var Dration: String,
    val Size: Int,
    val id: Long
)
