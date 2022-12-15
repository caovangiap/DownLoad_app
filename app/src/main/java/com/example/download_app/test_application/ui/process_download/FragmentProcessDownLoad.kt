package com.example.download_app.test_application.ui.process_download

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.download_app.test_application.ui.MainActivity
import com.example.download_app.R
import com.example.download_app.databinding.FragmentProcessDownloadBinding
import com.example.download_app.test_application.viewmodel.ViewModelDownLoad

class FragmentProcessDownLoad : Fragment() {

    lateinit var mView: View
    lateinit var binding: FragmentProcessDownloadBinding
    lateinit var viewModel: ViewModelDownLoad
    lateinit var adapter: AdapterProcessDownLoad

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProcessDownloadBinding.inflate(inflater, container, false)
        mView = binding.root
        viewModel = MainActivity.vModelDownLoad
        allFuncition()
        return mView
    }

    fun allFuncition() {
        setUpToolBar()
        đisplayProcess()
    }

    fun setUpToolBar() {
        binding.toolbar.inflateMenu(R.menu.alltoolbar)
        binding.toolbar.setNavigationIcon(R.drawable.back)
//yêu cầu activity điều hướng quay trở lại man manager
        binding.toolbar.setNavigationOnClickListener {
            viewModel.nextAction.postValue("Manager")
        }
    }

    /**
     * hiển thị tiến trình download
     */
    @SuppressLint("NotifyDataSetChanged")
    fun đisplayProcess() {
        adapter = AdapterProcessDownLoad(viewModel)
        adapter.oldDataProcess= viewModel.dataProcessDownLoad
        binding.pendingDownLoad.adapter = adapter
        binding.pendingDownLoad.layoutManager =
            GridLayoutManager(context, 2, LinearLayoutManager.VERTICAL, false)

        // cập nhật process từ viewmodel đẩy lên recycler view
        viewModel.liveDataProcessDownLoad.observe(viewLifecycleOwner) {
            adapter.oldDataProcess = it
            adapter.notifyItemRangeChanged(0,viewModel.position)
        }
    }
}


