package com.example.download_app.core_download.core

import android.content.Context
import android.util.Log
import android.util.Pair
import com.example.download_app.core_download.helper.MimeHelper
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL

class DownLoadTask(
    val url: String,
//    val context: WeakReference<Context>,
  //  val dao: DownloaderDao? = null,
    val downloadDir: String? = null,
    val timeOut: Int = 0,
//    val downloadListener: FuntionControl? = null,
    val header: Map<String, String>? = null,
    var fileName: String? = null,
    var extension: String? = null
) : FuntionControl {

    // region field
    internal var resume: Boolean = false
    private var connection: HttpURLConnection? = null
    private var downloadedFile: File? = null
    private var downloadedSize: Int = 0
    private var percent: Int = 0
    private var totalSize: Int = 0
    // endregion


    override fun onStartDownload() {
        try {
            val mUrl = URL(url)
            // open connection
            connection = mUrl.openConnection() as HttpURLConnection
            connection?.doInput = true
            connection?.readTimeout = timeOut
            connection?.connectTimeout = timeOut
            connection?.instanceFollowRedirects = true
            connection?.requestMethod = "GET"

            //set header request
            if (header != null) {
                for ((key, value) in header) {
                    connection?.setRequestProperty(key, value)
                }
            }

            // check file resume able if true set last size to request header
//            if (resume) {
//                val model = dao?.getDownloadByUrl(url)
//                percent = model?.percent!!
//                downloadedSize = model.size
//                totalSize = model.totalSize
//                connection?.allowUserInteraction = true
//                connection?.setRequestProperty("Range", "bytes=" + model.size + "-")
//            }

            connection?.connect()

            // get filename and file extension
            detectFileName()

            // check file size
            if (!resume) totalSize = connection?.contentLength!!

            // downloaded file
            downloadedFile = File(downloadDir + File.separator + fileName + "." + extension)

            Log.d("path",downloadDir + File.separator + fileName + "." + extension)

            // check file completed
            if (downloadedFile!!.exists() && downloadedFile?.length() == totalSize.toLong()) {
                Log.d("download_task","Complete")
            }

            // buffer file from input stream in connection
            val bufferedInputStream = BufferedInputStream(connection?.inputStream)
            val fileOutputStream =
                if (downloadedSize == 0) FileOutputStream(downloadedFile)
                else FileOutputStream(downloadedFile, true)

            val bufferedOutputStream = BufferedOutputStream(fileOutputStream, 1024)

            val buffer = ByteArray(32 * 1024)
            var len: Int
            var previousPercent = -1

            // update percent, size file downloaded
//            while (bufferedInputStream.read(buffer, 0, 1024).also { len = it } >= 0 && !isCancelled) {
//                if (!ConnectCheckerHelper.isInternetAvailable(context.get()!!)) {
//                    return Pair(false, IllegalStateException("Please check your network!"))
//                }
//                bufferedOutputStream.write(buffer, 0, len)
//                downloadedSize = downloadedSize.plus(len)
//                percent = (100.0f * downloadedSize.toFloat() / totalSize.toLong()).toInt()
//                if (previousPercent != percent) {
//                    downloadListener?.onProgressUpdate(percent, downloadedSize, totalSize)
//                    previousPercent = percent
//                    dao?.updateDownload(url, StatusModel.DOWNLOADING, percent, downloadedSize, totalSize)
//                }
//            }

            // close stream and connection
            bufferedOutputStream.flush()
            bufferedOutputStream.close()
            bufferedInputStream.close()
            connection?.disconnect()

        } catch (e: Exception) {
            connection?.disconnect()

        }
    }

    override fun onPause() {
        Log.d("Download_Task","Pause")
    }

    override fun onResume() {
        Log.d("Download_Task","Resume")
    }

    override fun onProgressUpdate(percent: Int, downloadedSize: Int, totalSize: Int) {
        Log.d("Download_Task","UpdateProcess")
    }

    override fun onCompleted(file: File?) {
        Log.d("Download_Task","Complete")
    }

    override fun onFailure(reason: String?) {
        Log.d("Download_Task","Failure")
    }

    override fun onCancel() {
        Log.d("Download_Task","Cancel")
    }


    private fun detectFileName() {
        val contentType = connection?.getHeaderField("Content-Type").toString()
        if (fileName == null) {

            val raw = connection?.getHeaderField("Content-Disposition")
            if (raw?.indexOf("=") != -1) {
//                fileName =
//                    raw?.split("=".toRegex())?.dropLastWhile { it.isEmpty() }
//                        ?.toTypedArray()?.get(1)
//                        ?.replace("\"", "")
                //  fileName = fileName?.lastIndexOf(".")?.let { fileName?.substring(0, it) }
                fileName = "test.mp4"
            }

            if (fileName == null) {
//                fileName = url.substringAfterLast("/")
//                fileName = fileName?.lastIndexOf(".")?.let { fileName?.substring(0, it) }
                fileName = "test.mp4"
            }

            fileName =
                if (fileName == null) System.currentTimeMillis().toString()
                else fileName

            extension = MimeHelper.guessExtensionFromMimeType(contentType)
        }
    }

}