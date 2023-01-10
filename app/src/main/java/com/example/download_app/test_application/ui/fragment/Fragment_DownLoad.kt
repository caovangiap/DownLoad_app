package com.example.download_app.test_application.ui.fragment


import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.download_app.databinding.FragmentDownloadBinding
import com.example.download_app.test_application.ui.MainActivity
import com.example.download_app.test_application.viewmodel.ViewModelDownLoad
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.internal.wait
import java.io.File


/**
 *  fragment phát action tải app + show storage
 */

class Fragment_DownLoad : Fragment() {

    lateinit var binding: FragmentDownloadBinding
    lateinit var viewModel: ViewModelDownLoad
    lateinit var mView: View

    private var fileName: String = ""

    // url của youtube
    private var UrlDownload: String = ""

    // folder mặc định để download
    private var nameOfFolderDownLoad: String = "/Download_app"


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = MainActivity.vModelDownLoad
        binding = FragmentDownloadBinding.inflate(inflater, container, false)
        mView = binding.root

        /**
         * reqquest quyen can dung trong fragment
         */

        checkPermissionForDownloadFragment()
        allFunticion()
        return mView
    }


    val checkPermissionWriteStorage = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Boolean
        if (isGranted) {
            Toast.makeText(context, "Permission DownlLoad: Success", Toast.LENGTH_SHORT)
        } else {
            Toast.makeText(context, "Permission DownlLoad: Deny", Toast.LENGTH_SHORT)
        }
    }

    /**
     *  ghi dữ liệu với android 10 trở xuống
     */
    fun checkPermissionForDownloadFragment() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            checkPermissionWriteStorage.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

// choose folder download cho user
//    /**
//     * chọn folder để download để lưu file sau download
//     */
//    fun chooseFolder(fileName: String) {
//        if (fileName != "") {
//            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
//            }
//            chooseFolder.launch(intent)
//        } else {
//            Toast.makeText(context, "Bạn chưa đặt tên cho file", Toast.LENGTH_SHORT)
//                .show()
//        }
//    }
//
//    /**
//     * lấy ra uri folder từ intent cho phép download xuống
//     */
//    val chooseFolder =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
//            it.data?.data.let {
//                if (it != null) {
//                    copyFileToExternalStorage(it.encodedPath)
//                }
//            }
//        }
//
//    /**
//     *  use ContentResolver viết file bằng uri
//     */
//    private fun copyFileToExternalStorage(destination: String?) {
//        val startDownload =
//            viewModel.getDownloader(
//                fileName,
//                "",
//                resourceUrlDownload,
//                requireContext()
//            )
//        startDownload.download()
//    }

    fun allFunticion() {
        binding.downloadBt.setOnClickListener {
            checkFolderExist()
        }
    }

    // check folder exist và tải về file
    fun checkFolderExist() {
        UrlDownload = "https://www.youtube.com/watch?v=QadaRNZS2Ms"
        val file =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + nameOfFolderDownLoad)

        if (file.exists() && file.isDirectory) {
            if (UrlDownload != "") {
                viewModel.getUrlVideo(UrlDownload).toString()
                downloadFile()
            }
        } else {
            file.mkdir()
            viewModel.getUrlVideo(UrlDownload).toString()
            downloadFile()
        }
    }

    fun downloadFile() {
        fileName = binding.urlVideo.text.toString()
        viewModel.resourceUrlYoutube.observe(viewLifecycleOwner) {
            val startDownload =
                viewModel.getDownloader(
                    fileName,
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + nameOfFolderDownLoad,
                    it,
                    requireContext()
                )
            startDownload.download()
        }

    }

}