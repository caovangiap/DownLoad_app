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
import com.example.download_app.core_download.DownloaderFromUrl
import com.example.download_app.core_download.core.FuntionControl
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

// Header class
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
     * instantce của class downlaad
     */
    private var downloader: DownloaderFromUrl? = null

    lateinit var data : DownLoadProcess

// Main Code


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
                    buildUpDownLoad(context,result.videoStreams[2].content)
                }
            })
            { throwable: Throwable? ->
                Log.d("false", "false")
            }
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
     * downlaod funcition
     */
    fun buildUpDownLoad(context: Context, url: String){

        data = DownLoadProcess("",0,url,"","","")
        dataProcessDownLoad.add(data)
       downloader = DownloaderFromUrl.Builder(
            context,
            url
        )
            .downloadListener(object : FuntionControl {
                override fun onStart() {
                    Log.d("ViewModel_DownLoad", "onStart")
                    dataProcessDownLoad.find { it.uri == url }?.status ="onStart"
                    liveDataProcessDownLoad.postValue(dataProcessDownLoad)
                }

                override fun onPause() {
                    dataProcessDownLoad.find { it.uri == url }?.status= "onPause"
                    Log.d("ViewModelDownLoad", "onPause")
                    liveDataProcessDownLoad.postValue(dataProcessDownLoad)
                }

                override fun onResume() {
                    dataProcessDownLoad.find { it.uri == url }?.status= "onResume"
                    Log.d("ViewModelDownLoad", "onResume")
                    liveDataProcessDownLoad.postValue(dataProcessDownLoad)
                }

                override fun onProgressUpdate(percent: Int, downloadedSize: Int, totalSize: Int) {

                    dataProcessDownLoad.find { it.uri == url }?.status = "onProgressUpdate"
                    dataProcessDownLoad.find { it.uri == url }?.textPercent = percent.toString().plus("%")
                    dataProcessDownLoad.find { it.uri == url }?.textPercentByte = getSize(downloadedSize)
                    dataProcessDownLoad.find { it.uri == url }?.totalSizeFile = getSize(totalSize)
                    dataProcessDownLoad.find { it.uri == url }?.intPercent = percent

                    Log.d(
                        "ViewModelDownLoad",
                        "onProgressUpdate: percent --> $percent downloadedSize --> $downloadedSize totalSize --> $totalSize "
                    )
                    liveDataProcessDownLoad.postValue(dataProcessDownLoad)
                }

                override fun onCompleted(file: File?) {
                    dataProcessDownLoad.find { it.uri == url }?.status= "onCompleted"
                    Log.d("ViewModelDownLoad", "onCompleted: file --> $file")
                    liveDataProcessDownLoad.postValue(dataProcessDownLoad)
                }

                override fun onFailure(reason: String?) {
                    dataProcessDownLoad.find { it.uri == url }?.status= "onFailure: reason --> $reason"
                    Log.d("ViewModelDownLoad", "onFailure: reason --> $reason")
                    liveDataProcessDownLoad.postValue(dataProcessDownLoad)
                }

                override fun onCancel() {
                    dataProcessDownLoad.find { it.uri == url }?.status= "onCancel"
                    Log.d("ViewModelDownLoad", "onCancel")
                    liveDataProcessDownLoad.postValue(dataProcessDownLoad)
                }
            }).build()
        downloader?.download()
    }

    /**
     * chuyển đổi định dạng Int sang byte
     */
    fun getSize(size: Int): String {
        var s = ""
        val kb = (size / 1024).toDouble()
        val mb = kb / 1024
        if (size < 1024) {
            s = "$size Bytes"
        } else if ( size < 1024 * 1024) {
            s = String.format("%.2f", kb) + " KB"
        } else if ( size < 1024 * 1024 * 1024) {
            s = String.format("%.2f", mb) + " MB"
        }
        return s
    }

}



