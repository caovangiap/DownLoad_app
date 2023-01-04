package com.example.download_app.test_application.ui.download


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
import com.example.download_app.databinding.FragmentDownloadBinding
import com.example.download_app.test_application.ui.MainActivity
import com.example.download_app.test_application.viewmodel.ViewModelDownLoad
import java.io.File


/**
 *  fragment phát action tải app + show storage
 */

class Fragment_DownLoad : Fragment() {

    lateinit var binding: FragmentDownloadBinding
    lateinit var viewModel: ViewModelDownLoad
    lateinit var mView: View

    private var fileName: String = ""

    // lấy ra đường dẫn tài nguyên url của youtube
    private var resourceUrlDownload: String? = null

    // folder mặc định để download
    private var nameOfFolderDownLoad: String = "/Download_app"


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDownloadBinding.inflate(inflater, container, false)
        mView = binding.root
        viewModel = MainActivity.vModelDownLoad
        /**
         * reqquest quyen can dung trong fragment
         */

        checkPermissionForDownloadFragment()
        allFuncition()
        return mView
    }

    fun allFuncition() {

        /**
         * click tai video
         */
        resourceUrlDownload = viewModel.getUrlVideo(
            "https://www.youtube.com/watch?v=5uTz-wgV9Dg&t=11s",
        )
        binding.download.setOnClickListener {
            //chọn local file to download
            fileName = binding.myUrl.text.toString()
            resourceUrlDownload = viewModel.getUrlVideo(
                "https://www.youtube.com/watch?v=5uTz-wgV9Dg&t=11s",
            )
            checkFolderExist()
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


    // check folder exist và tải về file
    fun checkFolderExist() {
        val file =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + nameOfFolderDownLoad)

        if (file.exists() && file.isDirectory) {
            if (fileName != "") {
                downloadFile()
            }
        } else {
            file.mkdir()
            downloadFile()
        }
    }

    fun downloadFile() {
        if (fileName != "") {
            val startDownload =
                viewModel.getDownloader(
                    fileName,
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + nameOfFolderDownLoad,
                    resourceUrlDownload,
                    requireContext()
                )
            startDownload.download()
            Toast.makeText(context, "video nằm trong file Download_app", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(context, "Bạn chưa đặt tên cho file dowwnload", Toast.LENGTH_SHORT)
                .show()
        }
    }

}