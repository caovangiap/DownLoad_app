package com.example.download_app

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.download_app.ui.download.FragmentShowStorage
import com.example.download_app.ui.download.Fragment_DownLoad
import com.example.download_app.ui.process_download.FragmentProcessDownLoad
import com.example.download_app.viewmodel.ViewModelDownLoad
import us.shandian.giga.settings.NewPipeSettings

class MainActivity : AppCompatActivity() {
    companion object{
        lateinit var vModelDownLoad : ViewModelDownLoad
        lateinit var ApplicationContext : Context
        val tagFragmentShowStorage = "ID_FRAGMENT_SHOW_STORAGE"
        val tagFragmentProcessDownLoad ="ID_FRAGMENT_PROCESS_DOWNLOAD"

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        vModelDownLoad = ViewModelDownLoad()
        NewPipeSettings.initSettings(this)
        ApplicationContext = this
        // All funcition
        allFuncition()
        fDownLoad()

    }

    fun fDownLoad(){
        val manager = supportFragmentManager.beginTransaction()
        val fragment = Fragment_DownLoad()
        manager.replace(R.id.Content,fragment)
        manager.commit()

    }

    fun allFuncition(){
        // funcition fragment downLoad
        funcitioonFragmentDownLoad()

    }


    fun funcitioonFragmentDownLoad(){
        // function downLoad
        fDownLoad()
        // function ShowStorage
        fShowStorage()
    }

    // lay du lieu tu kho external
    fun fShowStorage(){
        vModelDownLoad.nextAction.observe(this){
            when(it){
                vModelDownLoad.ACTION_SHOW_Storage -> {
                    val manager = supportFragmentManager.beginTransaction()
                    val fragmentShowStorage = FragmentShowStorage()
                    manager.replace(R.id.Content, fragmentShowStorage,tagFragmentShowStorage)
                    manager.commit()
                }
                "Manager"->{
                    val manager = supportFragmentManager.beginTransaction()
                    val fragment = Fragment_DownLoad()
                    manager.replace(R.id.Content,fragment)
                    manager.commit()
                }
                vModelDownLoad.ACTION_PROCESS_DOWNLOAD->{
                    val manager = supportFragmentManager.beginTransaction()
                    val fragment = FragmentProcessDownLoad()
                    manager.replace(R.id.Content,fragment, tagFragmentProcessDownLoad)
                    manager.commit()
                }
            }
        }
    }

}