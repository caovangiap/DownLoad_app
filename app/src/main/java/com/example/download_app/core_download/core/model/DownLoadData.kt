package com.example.download_app.core_download.core.model

internal data class DownLoadData(val url: String?,
                        val filename: String?,
                        val status: Int,
                        val percent: Int,
                        val size: Int,
                        val totalSize: Int)




