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
    private var resourceUrlDownload: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDownloadBinding.inflate(inflater, container, false)
        mView = binding.root
        viewModel = MainActivity.vModelDownLoad

        allFuncition()
        return mView
    }

    fun allFuncition() {
        /**
         * lấy ra url tài nguyên từ đường dẫn url gốc
         */

        resourceUrlDownload = viewModel.getUrlVideo(
            "https://www.youtube.com/watch?v=Roh8cCJ_qKw",
        )
        /**
         * click tai video
         */


        binding.download.setOnClickListener {
            fileName = binding.myUrl.text.toString()
            if (fileName != "") {
                resourceUrlDownload = viewModel.getUrlVideo(
                    "https://www.youtube.com/watch?v=Roh8cCJ_qKw",
                )
                if (resourceUrlDownload != null) {
                    downloader =
                        viewModel.getDownloader(fileName, resourceUrlDownload, requireContext())
                    downloader!!.download()
                }
            } else {
                Toast.makeText(context, "Vui lòng đặt tên cho video", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        /**
         *  hien thi toan bo video download
         */

        binding.AllVideo.setOnClickListener {
//            viewModel.nextAction.value = viewModel.ACTION_SHOW_Storage
            downloader?.pauseDownload()
        }

        /**
         * tien trình downLoad
         */
        binding.ProcessDownLoad.setOnClickListener {
 //           viewModel.nextAction.value = viewModel.ACTION_PROCESS_DOWNLOAD
            downloader =
                viewModel.getDownloader(fileName, resourceUrlDownload, requireContext())
            downloader?.resumeDownload()
        }
    }
}