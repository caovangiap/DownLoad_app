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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.download_app.databinding.FragmentDownloadBinding
import com.example.download_app.test_application.model.DownloadAdapter
import com.example.download_app.test_application.ui.MainActivity
import com.example.download_app.test_application.ui.adapter.AdapterFragmentDownLoad
import com.example.download_app.test_application.viewmodel.ViewModelDownLoad
import com.muicvtools.mutils.downloads.DailymtVideoFetch
import com.muicvtools.mutils.downloads.FetchListener
import com.muicvtools.mutils.downloads.StreamOtherInfo
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


    /**
     * chọn folder để lưu file sau download
     */
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
        setUpUi()
    }

    // check folder exist và tải về file
    fun checkFolderExist() {
        UrlDownload = "https://www.dailymotion.com/video/x6emb2z"
        val file =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + nameOfFolderDownLoad)

        if (file.exists() && file.isDirectory) {
            if (UrlDownload != "") {
                getUrlResource()
                downloadFile()
            }
        } else {
            file.mkdir()
            getUrlResource()
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

    fun getUrlResource(){

        DailymtVideoFetch.getVideo("https://www.dailymotion.com/video/x8h58zi?playlist=x6hzkz",object : FetchListener {
            override fun requireLogin() {

            }
            override fun onFetchedSuccess(detail: StreamOtherInfo?) {
                val size = detail?.urls_stream?.size
                val imageThumball = detail?.url_thumb
                val detailItems = detail?.urls_stream
                val title = detail?.title_file
                val items = DownloadAdapter(size!!,imageThumball!!,detailItems,title!!)
                viewModel.dataChooserQualityItems.postValue(items)
            }
            override fun onFetchedFail(message: String?) {
            }
        })
    }

    fun setUpUi(){
        viewModel.dataChooserQualityItems.observe(viewLifecycleOwner){
            binding.viewQuality.adapter = AdapterFragmentDownLoad(it,viewModel)
            binding.viewQuality.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        }
    }

}