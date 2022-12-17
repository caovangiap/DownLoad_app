package com.example.download_app.test_application.viewmodel
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.download_app.test_application.model.DownLoadProcess
import com.example.download_app.test_application.ui.MainActivity
import com.example.download_app.test_application.model.StorageData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import org.schabi.newpipe.extractor.stream.StreamInfo
import us.shandian.giga.util.ExtractorHelper
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit




/**
 * Xử lý mọi logic cho chức năng download va chức năng show video
 */
class ViewModelDownLoad : ViewModel() {

    // header khai báo biến
    /** action di chuyển các màn
     *
     */
    val ACTION_SHOW_Storage = "ACTION_SHOW_STORAGE_DOWNLOAD"
    val ACTION_PROCESS_DOWNLOAD = "DISPLAY_PROCESS_DOWNLOAD"
    val nextAction = MutableLiveData<String>()

    /**
     * live data storage hiển thị recycle
     */
    val allVideoStorage = MutableLiveData<MutableList<StorageData>>()

    /**
     * object là 1 list các tiến trình download
     */

    val dataProcessDownLoad = mutableListOf<DownLoadProcess>()
    val liveDataProcessDownLoad = MutableLiveData<MutableList<DownLoadProcess>>()

    /**
     * vị trí items thay đổi
     */
    var position = 0

    /**
     * đường dẫn tài nguyên youtube
     */
     var resourceUrlYoutube : String? = null


    // main code
    /**
     *  get url youtube cho vao StartDownLoad
     */
    fun getUrlVideo(url: String): String? {
        // thư viện nhận url của youtube thông qua thư viện
        ExtractorHelper.getStreamInfo(0, url, true)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                    result: StreamInfo? ->
                if (result != null) {

                    // video co am thanh nhung max la 720p
                    Log.d("video Stream", result.videoStreams[2].content)
                    Log.d("video Stream", result.videoStreams[0].content)
                    Log.d("video Stream", result.videoStreams[1].content)
                    resourceUrlYoutube = result.videoStreams[1].content
                }
            })
            { throwable: Throwable? ->
                Log.d("false", "false")
            }
        return resourceUrlYoutube
    }



    /**
     * get data from storage
     */
    fun getDataStorage(): MutableList<StorageData> {

        val videoList = mutableListOf<StorageData>()

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.SIZE
        )

// Show only videos that are at least 5 minutes in duration.
        val selection = "${MediaStore.Video.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(
            TimeUnit.MILLISECONDS.convert(9, TimeUnit.MINUTES).toString()
        )

// Display videos in alphabetical order based on their display name.
        val sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} ASC"

        val query = MainActivity.ApplicationContext.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )
        query?.use { cursor ->
            // địa chỉ hàng cột chứa dữ liệu trên data storage
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

            while (cursor.moveToNext()) {
                // lấy dữ liệu video tại hàng cột được chỉ định.
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getInt(sizeColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                val date = convertToDate(duration, "yyyy-MM-dd HH:mm:ss")

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                videoList += StorageData(contentUri, name, date, size, id)
            }
            allVideoStorage.value = videoList
        }
        return videoList
    }

    /**
     * chuyển đổi định dạng ngày tháng năm sau khi lấy dữ liệu từ storage
     */
    private fun convertToDate(milliSeconds: Long, dateFormat: String): String {
        val formatter = SimpleDateFormat(dateFormat)
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds * 1000
        return formatter.format(calendar.time)
    }


    /**
     * chỉnh sửa thông tin file
     */
    fun updateVideo(mediaId: Long, updateName: String, myFavoriteSongUri: Uri) {

        // Updates an existing media item.
        val resolver = MainActivity.ApplicationContext.contentResolver

        // When performing a single item update, prefer using the ID
        val selection = "${MediaStore.Audio.Media._ID} = ?"

        // By using selection + args we protect against improper escaping of // values.
        val selectionArgs = arrayOf(mediaId.toString())

        // Update an existing song.
        val updatedSongDetails = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, updateName)
        }

// Use the individual song's URI to represent the collection that's
// updated.

        resolver.update(
            myFavoriteSongUri,
            updatedSongDetails,
            selection,
            selectionArgs
        )
    }

    /**
     * Xóa file external
     */
    fun deleteVideo(mediaId: Long, myFavoriteSongUri: Uri) {

        // Updates an existing media item.
        val resolver = MainActivity.ApplicationContext.contentResolver

        // When performing a single item update, prefer using the ID
        val selection = "${MediaStore.Audio.Media._ID} = ?"

        // By using selection + args we protect against improper escaping of // values.
        val selectionArgs = arrayOf(mediaId.toString())


// Use the individual song's URI to represent the collection that's
// updated.
        resolver.delete(
            myFavoriteSongUri,
            selection,
            selectionArgs
        )

    }

    /**
     * tổng dung lượng file download chuyển đổi sang MB Gb hoặc ...
     */
    private fun bytesIntoHumanReadable(bytes: Long): String {
        val kilobyte: Long = 1024
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024
        val terabyte = gigabyte * 1024
        return if (bytes >= 0 && bytes < kilobyte) {
            "$bytes B"
        } else if (bytes >= kilobyte && bytes < megabyte) {
            (bytes / kilobyte).toString() + " KB"
        } else if (bytes >= megabyte && bytes < gigabyte) {
            (bytes / megabyte).toString() + " MB"
        } else if (bytes >= gigabyte && bytes < terabyte) {
            (bytes / gigabyte).toString() + " GB"
        } else if (bytes >= terabyte) {
            (bytes / terabyte).toString() + " TB"
        } else {
            "$bytes Bytes"
        }
    }
}



