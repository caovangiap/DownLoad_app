package com.example.download_app.test_application.viewmodel
import android.app.DownloadManager
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.CookieManager
import android.webkit.URLUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.download_app.ConstantDownLoadApp
import com.example.download_app.test_application.ui.MainActivity
import com.example.download_app.test_application.model.ProcessData
import com.example.download_app.test_application.model.StorageData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.stream.StreamInfo
import us.shandian.giga.util.ExtractorHelper
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit




/**
 * Xử lý mọi logic cho chức năng download va chức năng show video
 */
class ViewModelDownLoad : ViewModel() {


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

    val dataProcessDownLoad = mutableListOf<ProcessData>()
    val liveDataProcessDownLoad = MutableLiveData<MutableList<ProcessData>>()

    /**
     * vị trí items thay đổi
     */
    var position = 0
    /**
     *  get url youtube cho vao StartDownLoad
     */
    fun getUrlVideo(url: String, context: Context) {
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
                    /**
                     * bắt đầu download
                      */
//                    startDownLLoad(
//                        context,
//                        result.videoStreams[2].content
//                    )

                }
            })
            { throwable: Throwable? ->
                Log.d("false", "false")
            }
    }

    /**
     *  downLoad khi co url cua video
     */
    fun startDownLLoad(context: Context, uri: String) {

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadReference: Long
        val fileName = URLUtil.guessFileName(uri, "${position}", "${position}")
        val cookie = CookieManager.getInstance().getCookie(uri)
        val downloadPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        val file = File(downloadPath, fileName)
        val request = DownloadManager.Request(Uri.parse(uri))


        request.setTitle(fileName)
        request.setDescription("${position}")
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
        request.addRequestHeader("cookie", cookie)
        request.setDestinationUri(Uri.fromFile(file))
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_MOVIES,
            fileName
        )
        downloadReference = downloadManager.enqueue(request)
        /**
         *  hàm chứa tiến trình download ( dataProcessDownLoad lưu id các tiến trình để hiển thị recycler view )
         */
        // các items view tiến trình download được add vào mảng dataProcessDownLoad cập nhật lên
        // recyclerview ngay sau click startDownLoad
        dataProcessDownLoad.add(
            ProcessData(
                downloadReference,
                "$position",
                0,
                uri,
                ConstantDownLoadApp.actionRunning,
                fileName,
                "Wait Loading",
                "0MB",
                ConstantDownLoadApp.actionResume
            )
        )
        liveDataProcessDownLoad.value = dataProcessDownLoad
        getMessageProcessOrStatus(downloadReference, downloadManager, dataProcessDownLoad, position)
        position += 1
        
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
     * hien thi tien trinh download và status download
     */
    fun getMessageProcessOrStatus(
        downloadId: Long,
        downloadManager: DownloadManager,
        dataProcessDownLoad: MutableList<ProcessData>,
        position: Int
    ) {

        viewModelScope.launch(Dispatchers.IO) {
            try {
                while (dataProcessDownLoad[position].conditionDownLoad == ConstantDownLoadApp.actionRunning) {
                    val query = DownloadManager.Query()
                    // set the query filter to our previously Enqueued download
                    query.setFilterById(downloadId)
                    // con trỏ truy vấn quá trình tải xuống.
                    val cursor = downloadManager.query(query)
                    if (cursor.moveToFirst()) {
                        /**
                         * cập nhật status các tình huống khi download
                         */
                        getStatusMessage(cursor, dataProcessDownLoad[position])
                        // this "if" is crucial to prevent a kind of error
                        val downloadedBytes =
                            cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val totalBytes =
                            cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)) // integer is enough for files under 2GB
                        val downloadProgress = downloadedBytes * 100f / totalBytes
                        if (downloadProgress > 99.99999999) {
                            // process
                            dataProcessDownLoad[position].conditionDownLoad = ConstantDownLoadApp.actionDownLoadComplete
                            dataProcessDownLoad.find { it.id_ProcessDownLoad == downloadId }?.intPercent =
                                Math.floor(downloadProgress.toDouble()).toInt()
                            dataProcessDownLoad[position].totalSizeFile = totalBytes.toString()
                        } else {
                            /**
                             * tien trình download
                             */
                            viewModelScope.launch(Dispatchers.Main) {
                                // cập nhật % download từ phần tử trùng với id_ProcessDownLoad
                                dataProcessDownLoad.find { it.id_ProcessDownLoad == downloadId }?.intPercent =
                                    Math.floor(downloadProgress.toDouble()).toInt()
                                dataProcessDownLoad[position].totalSizeFile =
                                    bytesIntoHumanReadable(totalBytes)
                                // live data len ui
                                liveDataProcessDownLoad.postValue(dataProcessDownLoad)
                            }
                        }
                        cursor.close()
                    }
                }
            } catch (System: java.lang.IllegalStateException) {
                downloadManager.remove(downloadId)
            }
        }
    }

    private fun getStatusMessage(cursor: Cursor, processData: ProcessData): String? {
        var msg = "-"
        val reason =
            cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
        when (cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
            DownloadManager.STATUS_FAILED -> {
                when (reason) {
                    DownloadManager.ERROR_CANNOT_RESUME -> {
                        msg = "ERROR_CANNOT_RESUME"
                    }
                    DownloadManager.ERROR_DEVICE_NOT_FOUND -> {
                        msg = "ERROR_DEVICE_NOT_FOUND"
                    }

                    DownloadManager.ERROR_FILE_ALREADY_EXISTS -> {
                        msg = "ERROR_FILE_ALREADY_EXISTS"
                    }
                    DownloadManager.ERROR_FILE_ERROR -> {
                        msg = "ERROR_FILE_ERROR"
                    }
                    DownloadManager.ERROR_HTTP_DATA_ERROR -> {
                        msg = "ERROR_HTTP_DATA_ERROR"
                    }
                    DownloadManager.ERROR_INSUFFICIENT_SPACE -> {
                        msg = "ERROR_INSUFFICIENT_SPACE"
                    }
                    DownloadManager.ERROR_TOO_MANY_REDIRECTS -> {
                        msg = "ERROR_TOO_MANY_REDIRECTS"
                    }
                    DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> {
                        msg = "ERROR_UNHANDLED_HTTP_CODE"
                    }
                    DownloadManager.ERROR_UNKNOWN -> msg = "ERROR_UNKNOWN"
                }
            }
            DownloadManager.STATUS_PAUSED -> {

            when (reason) {
                DownloadManager.PAUSED_QUEUED_FOR_WIFI -> msg = "PAUSED_QUEUED_FOR_WIFI"
                DownloadManager.PAUSED_WAITING_TO_RETRY -> msg= "PAUSED_WAITING_TO_RETRY"
                DownloadManager.PAUSED_UNKNOWN -> msg = "PAUSED_UNKNOWN"
                DownloadManager.PAUSED_WAITING_FOR_NETWORK -> msg = "PAUSED_WAITING_FOR_NETWORK"
            }
        }
            DownloadManager.STATUS_RUNNING -> msg = "Running"
            DownloadManager.STATUS_SUCCESSFUL -> msg = "Completed"
            DownloadManager.STATUS_PENDING -> msg = "Pending"
            else -> msg = "Unknown"
        }


        processData.status = msg
        return msg
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



