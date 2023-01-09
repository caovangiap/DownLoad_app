package com.example.download_app.test_application.viewmodel

import caogiap.lib.downloader.core.OnDownloadListener
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.download_app.core_download.DownloaderFromUrl
import com.example.download_app.test_application.model.DownLoadProcess
import com.example.download_app.test_application.ui.MainActivity
import com.example.download_app.test_application.model.StorageData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import org.schabi.newpipe.extractor.stream.StreamInfo
import us.shandian.giga.util.ExtractorHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * Xử lý mọi logic cho chức năng download va chức năng show video
 */
class ViewModelDownLoad : ViewModel() {

    /**
     * header khai báo biến
      */
    // action di chuyển các màn

    val ACTION_SHOW_Storage = "ACTION_SHOW_STORAGE_DOWNLOAD"
    val ACTION_PROCESS_DOWNLOAD = "DISPLAY_PROCESS_DOWNLOAD"
    val nextAction = MutableLiveData<String>()

     // live data storage hiển thị recycle
    val allVideoStorage = MutableLiveData<MutableList<StorageData>>()


     // object là 1 list các tiến trình download
    val dataProcessDownLoad = mutableListOf<DownLoadProcess>()
    val liveDataProcessDownLoad = MutableLiveData<MutableList<DownLoadProcess>>()

     // vị trí items thay đổi
    var position = 0


    //đường dẫn tài nguyên youtube
    var resourceUrlYoutube: String? = null

    // livedata cảnh báo lỗi
    val bugDownloadFailer = MutableLiveData<String>()



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
                    resourceUrlYoutube = result.videoStreams[1].content
                    Log.d("url",result.videoStreams[1].content.toString())
                }
            })
            { throwable: Throwable? ->
                Log.d("false", "false")
            }
        return resourceUrlYoutube
    }

    /**
     * Mỗi lần gọi đến lớp này là đang tạo ra 1 thể hiện mới của downloader vì
     * DownloaderFromUrl.Buile là tạo ra 1 object khác
     */
    fun getDownloader(fileName: String,locationUrl: String, url: String?, context: Context): DownloaderFromUrl {

        val downloader = DownloaderFromUrl.Builder(
            context,
            url!!
        )
            .fileName(fileName,"mp4")
            .downloadDirectory(locationUrl)
            .downloadListener(object : OnDownloadListener {
                override fun onStart() {
                    Log.d("", "onStart")
                }

                override fun onPause() {
                    Log.d("", "onPause")
                }

                override fun onResume() {
                    Log.d("", "onResume")
                }

                override fun onProgressUpdate(
                    percent: Int,
                    downloadedSize: Int,
                    totalSize: Int
                ) {
                    Log.d(
                        "",
                        "onProgressUpdate: percent --> $percent downloadedSize --> $downloadedSize totalSize --> $totalSize "
                    )
                }

                override fun onCompleted(file: File?) {
                    Log.d("", "onCompleted: file --> $file")
                }

                override fun onFailure(reason: String?) {
                    Log.d("", "onFailure: reason --> $reason")
                    bugDownloadFailer.value = reason.toString()
                }

                override fun onCancel() {
                    Log.d("", "onCancel")
                }
            }).build()
        return downloader

    }

    /**
     * get data from storage
     */
    fun getDataStorage() {
        // android 10 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val videoList = mutableListOf<StorageData>()

            val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.SIZE
            )
            val query = MainActivity.ApplicationContext.contentResolver.query(
                collection,
                projection,
                null,
                null,
                null
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
                    var contentUri: Uri? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentUri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                    }

                    val date = convertToDate(duration, "yyyy-MM-dd HH:mm:ss")

                    // Stores column values and the contentUri in a local object
                    // that represents the media file.
                    videoList += StorageData(contentUri!!, name, date, size, id)
                }
                allVideoStorage.value = videoList
                Log.d("0",allVideoStorage.value.toString())
            }
        }
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
}



