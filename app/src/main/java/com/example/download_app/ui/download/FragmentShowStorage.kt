package com.example.download_app.ui.download

import android.Manifest
import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.download_app.MainActivity
import com.example.download_app.R
import com.example.download_app.databinding.FragmentShowStorageBinding

import com.example.download_app.viewmodel.ViewModelDownLoad


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
                    viewModel.updateVideo(idMediaUpdaterFragmentShowStorage!!,contentMediaUpdaterFragmentShowStorage!!,uriMediaUpdaterFragmentShowStorage!!)
                }
                /**
                 * Remove Items nếu được đồng ý
                 */
                if (adapterItemsStorage.ACTION_CODE == adapterItemsStorage.REMOVE_CODE){
                    viewModel.deleteVideo(idMediaUpdaterFragmentShowStorage!!,uriMediaUpdaterFragmentShowStorage!!)
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
        setUpToolBar()
        showStorage()
    }

    fun setUpToolBar() {
        binding.toolbar.inflateMenu(R.menu.alltoolbar)
        binding.toolbar.setNavigationIcon(R.drawable.back)
//yêu cầu activity điều hướng quay trở lại man manager
        binding.toolbar.setNavigationOnClickListener {
            viewModel.nextAction.postValue("Manager")
        }
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

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", context?.packageName, null)
                intent.data = uri
                StoragePermissionLauncherResults.launch(intent)

            } catch (e: java.lang.Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                StoragePermissionLauncherResults.launch(intent)
            }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // lay giu lieu storage
                    viewModel.getDataStorage()
                    Toast.makeText(context, "Permission Success 1", Toast.LENGTH_SHORT)
                } else {
                    Toast.makeText(context, "false Permission", Toast.LENGTH_SHORT)
                }
            }
        }

    /**
     *  hiển thị video trong storage
     */

    fun showVideoStorage() {
        val fragment = activity?.supportFragmentManager?.findFragmentByTag(MainActivity.tagFragmentShowStorage) as FragmentShowStorage
        viewModel.allVideoStorage.observe(viewLifecycleOwner) {
            adapterItemsStorage = AdapterShowStorage(it, viewModel,fragment)
            binding.allItems.adapter = adapterItemsStorage
            binding.allItems.layoutManager =
                GridLayoutManager(context, 2, LinearLayoutManager.VERTICAL, false)
        }
    }

    /**
     * lắng nghe sự kiện update items từ recyclerview AdapterShowStorage ( REMOVE OR EDIT )
     */

    fun updateItemsMediaRecycler(id: Long, Text: String, url: Uri, possition: Int) {
        idMediaUpdaterFragmentShowStorage = id
        uriMediaUpdaterFragmentShowStorage = url
        contentMediaUpdaterFragmentShowStorage = Text
        positionMediaUpdateofFragmentShowStorage= possition
        /**
         * lắng nghe sự kiện edit từ recycler view adapter
         */
        if (adapterItemsStorage.ACTION_CODE == adapterItemsStorage.EDIT_CODE){
            try {
                viewModel.updateVideo(
                    id,
                    Text,
                    url
                )
                adapterItemsStorage.itemsChange(
                    possition,Text)

                // khi update nhưng file external thhuoocj app khác
            } catch (securityException: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableSecurityException = securityException as?
                            RecoverableSecurityException
                        ?: throw RuntimeException(securityException.message, securityException)

                    val intentSender =
                        recoverableSecurityException.userAction.actionIntent.intentSender
                    intentSender.let { sender ->
                        intentSenderLaucher.launch(
                            IntentSenderRequest.Builder(sender).build()
                        )
                    }
                } else {
                    throw RuntimeException(securityException.message, securityException)
                }
            }
        }

        /**
         * lắng nghe sự kiện remove từ recycler view adapter
         */
        if (adapterItemsStorage.ACTION_CODE == adapterItemsStorage.REMOVE_CODE){
            try {
                viewModel.deleteVideo(id,url)
                adapterItemsStorage.removeItems(possition)

                // khi remove nhưng file external thhuoocj app khác
            } catch (securityException: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableSecurityException = securityException as?
                            RecoverableSecurityException
                        ?: throw RuntimeException(securityException.message, securityException)

                    val intentSender =
                        recoverableSecurityException.userAction.actionIntent.intentSender
                    intentSender.let { sender ->
                        intentSenderLaucher.launch(
                            IntentSenderRequest.Builder(sender).build()
                        )
                    }
                } else {
                    throw RuntimeException(securityException.message, securityException)
                }
            }
        }

    }



}