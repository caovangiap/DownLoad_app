package com.example.download_app.test_application.ui.download


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.download_app.databinding.FragmentDownloadBinding
import com.example.download_app.test_application.ui.MainActivity
import com.example.download_app.test_application.viewmodel.ViewModelDownLoad
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
            chooseFolder(fileName)
            resourceUrlDownload = viewModel.getUrlVideo(
                "https://www.youtube.com/watch?v=5uTz-wgV9Dg&t=11s",
            )
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

    /**
     * chọn folder để download để lưu file sau download
     */
    fun chooseFolder(fileName: String) {
        if (fileName!=""){
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
//                addCategory(Intent.CATEGORY_OPENABLE)
//                type = "*/*"
//                putExtra(Intent.EXTRA_TITLE, fileName)
            }
            chooseFolder.launch(intent)
        }else{
            Toast.makeText(context, "Bạn chưa đặt tên cho file", Toast.LENGTH_SHORT)
                .show()
        }
    }

    /**
     * lấy ra uri folder từ intent cho phép download xuống
     */
    val chooseFolder = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
        it.data?.data.let {
            if (it != null) {
                 copyFileToExternalStorage(it.toString())
            }
        }
    }
    // use ContentResolver viết file bằng uri
    private fun copyFileToExternalStorage(destination: String) {
            val startDownload =
                viewModel.getDownloader(
                    fileName,
                    destination,
                    resourceUrlDownload,
                    requireContext()
                )
            Log.d("results", resourceUrlDownload.toString())
            startDownload.download()
    }

}