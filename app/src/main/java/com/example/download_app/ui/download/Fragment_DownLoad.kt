package com.example.download_app.ui.download



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.download_app.MainActivity
import com.example.download_app.databinding.FragmentDownloadBinding
import com.example.download_app.viewmodel.ViewModelDownLoad

/**
 *  fragment phát action tải app + show storage
 */

class Fragment_DownLoad : Fragment() {

    lateinit var binding: FragmentDownloadBinding
    lateinit var viewModel: ViewModelDownLoad
    lateinit var mView: View
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
        // click tai video
        binding.download.setOnClickListener {
            val url = binding.myUrl.text.toString()
            if (url!=""){
                context?.let {
                    viewModel.getUrlVideo(
                        "https://www.youtube.com/watch?v=Roh8cCJ_qKw",
                        requireContext()
                    )
                }
            }
            else{
                Toast.makeText(context,"Vui lòng nhập đường dẫn video",Toast.LENGTH_SHORT)
                    .show()
            }
        }
        //hien thi toan bo video download
        binding.AllVideo.setOnClickListener {
            viewModel.nextAction.value = viewModel.ACTION_SHOW_Storage
        }
        //tieens trình downLoad
        binding.ProcessDownLoad.setOnClickListener {
            viewModel.nextAction.value = viewModel.ACTION_PROCESS_DOWNLOAD
        }

    }


}