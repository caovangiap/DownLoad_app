package com.example.download_app.test_application.ui.download


import alirezat775.lib.downloader.core.OnDownloadListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.download_app.core_download.DownloaderFromUrl
import com.example.download_app.test_application.ui.MainActivity
import com.example.download_app.databinding.FragmentDownloadBinding
import com.example.download_app.test_application.viewmodel.ViewModelDownLoad
import java.io.File


/**
 *  fragment phát action tải app + show storage
 */

class Fragment_DownLoad : Fragment() {

    lateinit var binding: FragmentDownloadBinding
    lateinit var viewModel: ViewModelDownLoad
    lateinit var mView: View
    private var downloader: DownloaderFromUrl? = null
    private var fileName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDownloadBinding.inflate(inflater, container, false)
        mView = binding.root
        viewModel = MainActivity.vModelDownLoad
        // funcition
        allFuncition()
        return mView
    }

    fun allFuncition() {
        /**
         * click tai video
         */
        binding.download.setOnClickListener {
            fileName = binding.myUrl.text.toString()
            if (fileName != "") {

                // lấy ra url tài nguyên từ đường dẫn url gốc
                val url = viewModel.getUrlVideo(
                    "https://www.youtube.com/watch?v=Roh8cCJ_qKw",
                    requireContext()
                )
                if (url != null) {
                    getUrlResource(fileName, url)
                }
                downloader?.download()
            } else {
                Toast.makeText(context, "Vui lòng nhập đường dẫn video", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        /**
         *  hien thi toan bo video download
         */

        binding.AllVideo.setOnClickListener {
            viewModel.nextAction.value = viewModel.ACTION_SHOW_Storage
        }
        /**
         * tien trình downLoad
         */
        binding.ProcessDownLoad.setOnClickListener {
            viewModel.nextAction.value = viewModel.ACTION_PROCESS_DOWNLOAD
        }

    }


    // set up object download
    fun getUrlResource(fileName: String, url: String) {
        downloader = DownloaderFromUrl.Builder(
            requireContext(),
            url
        )
            .fileName(fileName,"mp4")
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
                }

                override fun onCancel() {
                    Log.d("", "onCancel")
                }
            }).build()

    }

}