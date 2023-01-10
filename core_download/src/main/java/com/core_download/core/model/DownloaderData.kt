package caogiap.lib.downloader.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cao Giap 10/1/2023
 */

@Entity
internal data class DownloaderData(
    @PrimaryKey val id: Int,
    val url: String?,
    val filename: String?,
    val status: Int,
    val percent: Int,
    val size: Int,
    val totalSize: Int
)