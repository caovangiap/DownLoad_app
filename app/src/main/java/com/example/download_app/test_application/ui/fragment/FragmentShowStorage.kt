package com.example.download_app.test_application.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.download_app.test_application.ui.MainActivity
import com.example.download_app.databinding.FragmentShowStorageBinding
import com.example.download_app.test_application.ui.adapter.AdapterShowStorage
import com.example.download_app.test_application.viewmodel.ViewModelDownLoad


class FragmentShowStorage : Fragment() {
    lateinit var binding: FragmentShowStorageBinding
    lateinit var mView: View
    lateinit var viewModel: ViewModelDownLoad
    lateinit var adapterItemsStorage: AdapterShowStorage

    /**
     * thông tin items đăng nhập khi click items recycler view
     */
    // vị trí items
    var positionMediaUpdateofFragmentShowStorage : Int? = null
    // nội dung tên file mới
    var contentMediaUpdaterFragmentShowStorage : String? = null
    //id media được cập nhật
    var idMediaUpdaterFragmentShowStorage : Long? = null
    // uri media được cập nhật
    var uriMediaUpdaterFragmentShowStorage : Uri? = null


    // biến lắng nghe sự kiện nếu người dùng chọn don't ask again
    val checkDontAskAgain =
        shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    /**
     * cấp quyền đọc sửa file của app khác
     */
    val intentSenderLaucher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                Toast.makeText(MainActivity.ApplicationContext, " Edit_success", Toast.LENGTH_SHORT)
                    .show()
                /**
                 * update items nếu đc đồng ý
                 */
                if (adapterItemsStorage.ACTION_CODE == adapterItemsStorage.EDIT_CODE){
                    adapterItemsStorage.itemsChange(
                        positionMediaUpdateofFragmentShowStorage!!, contentMediaUpdaterFragmentShowStorage!!
                    )
                    adapterItemsStorage.updateVideo(idMediaUpdaterFragmentShowStorage!!,contentMediaUpdaterFragmentShowStorage!!,uriMediaUpdaterFragmentShowStorage!!)
                }
                /**
                 * Remove Items nếu được đồng ý
                 */
                if (adapterItemsStorage.ACTION_CODE == adapterItemsStorage.REMOVE_CODE){
                    adapterItemsStorage.deleteVideo(idMediaUpdaterFragmentShowStorage!!,uriMediaUpdaterFragmentShowStorage!!)
                    adapterItemsStorage.removeItems(positionMediaUpdateofFragmentShowStorage!!)
                }

            } else {
                Toast.makeText(MainActivity.ApplicationContext, " Edit_Failse", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = MainActivity.vModelDownLoad
        binding = FragmentShowStorageBinding.inflate(layoutInflater, container, false)
        mView = binding.root
        allFunction()
        return mView
    }

    fun allFunction() {
        showStorage()
    }

    fun showStorage() {
        //check quyền dùng dữ liệu
        CheckPermission()
        // hiển thị dữ liệu lên recycler view
        showVideoStorage()

    }

    /**
     * check quyền đọc ghi dữ liệu
     */


    fun CheckPermission() {

        /**
         * xin quyền với trường hợp từ android 10 trở lên
         */

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
//            try {
//                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                val uri = Uri.fromParts("package", context?.packageName, null)
//                intent.data = uri
//                StoragePermissionLauncherResults.launch(intent)
//                Log.d("","Lớn hơn 10")
//            } catch (e: java.lang.Exception) {
//                val intent = Intent()
//                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
//                StoragePermissionLauncherResults.launch(intent)
//                Log.d("","bé hơn 10")
//            }
            Log.d("Fragment_Show _Storage","Lớn hơn 10")
            viewModel.getDataStorage()
        }
        /**
         * dưới androis 10
         */

        else {
            // xin quyền khi chưa có
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /**
     *  sử lý action sau khi xin quyền với android < android 10
     */

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                viewModel.getDataStorage()

            } else {

                // xử lý trường hợp don's ask again
                if (!checkDontAskAgain) {
                    Toast.makeText(
                        context,
                        "hãy cấp quyền bộ nhớ để sử dụng chức năng này",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", context?.packageName, null)
                    intent.data = uri
                    StoragePermissionLauncherResults.launch(intent)
                }
                Toast.makeText(
                    context,
                    "hãy cấp quyền bộ nhớ để sử dụng chức năng này",
                    Toast.LENGTH_LONG
                )
            }
        }

    /**
     * xử lý action sau khi câp quyền --> thay the cho  start activityForResults
     */



    val StoragePermissionLauncherResults =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            // với các phiên bản từ 11 trở lên
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // lay giu lieu storage
                    viewModel.getDataStorage()
                    Toast.makeText(context, "Permission Success 1", Toast.LENGTH_SHORT)
                } else {
                    Toast.makeText(context, "false Permission", Toast.LENGTH_SHORT)
                }
            }
            // các phiên bản androoid 6 trở lên 11
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.R){
                    if (context?.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)  ==PackageManager.PERMISSION_GRANTED){
                        viewModel.getDataStorage()
                        Toast.makeText(context, "Permission Success 1", Toast.LENGTH_SHORT)
                    }
                    else {
                        Toast.makeText(context, "false Permission", Toast.LENGTH_SHORT)
                    }
                }
        }

    /**
     *  hiển thị video trong storage
     */

    fun showVideoStorage() {

        viewModel.allVideoStorage.observe(viewLifecycleOwner) {
            adapterItemsStorage = AdapterShowStorage(it)
            binding.allItems.adapter = adapterItemsStorage
            binding.allItems.layoutManager =
                GridLayoutManager(context, 2, LinearLayoutManager.VERTICAL, false)
        }
    }




}