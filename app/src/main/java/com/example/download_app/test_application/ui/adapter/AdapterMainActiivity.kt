package com.example.download_app.test_application.ui.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.download_app.ConstantDownLoadApp
import com.example.download_app.test_application.ui.fragment.FragmentProcessDownLoad
import com.example.download_app.test_application.ui.fragment.FragmentShowStorage
import com.example.download_app.test_application.ui.fragment.Fragment_DownLoad

/**
 * adapter viewpage + bottom navigation + navigation drawer
 */

class AdapterMainActiivity (activity : AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position){
            ConstantDownLoadApp.codeFragmentDownLoad->{
                Fragment_DownLoad()
            }
            ConstantDownLoadApp.codeFragmentProcess->{
                FragmentProcessDownLoad()
            }
            ConstantDownLoadApp.codeFragmentShowStorage->{
                FragmentShowStorage()
            }
            else ->Fragment_DownLoad()
        }
    }

}