package com.example.download_app.test_application.model

import com.muicvtools.mutils.downloads.VideoDetail
import java.util.ArrayList

data class DownloadAdapter(
    val numberItems: Int,
    val imageThumb: String,
    val downloadQuality: ArrayList<VideoDetail>?,
    val titleFile: String
)