package com.example.download_app.test_application.ui

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.viewpager2.widget.ViewPager2
import com.example.download_app.ConstantDownLoadApp
import com.example.download_app.R
import com.example.download_app.databinding.ActivityMainBinding
import com.example.download_app.test_application.ui.adapter.AdapterMainActiivity
import com.example.download_app.test_application.viewmodel.ViewModelDownLoad
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import us.shandian.giga.settings.NewPipeSettings

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var viewPage : AdapterMainActiivity
    companion object{
        lateinit var vModelDownLoad : ViewModelDownLoad
        lateinit var ApplicationContext : Context
    }

    // fragment hiện tại
    var codeCurrentFragment = ConstantDownLoadApp.codeFragmentDownLoad


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NewPipeSettings.initSettings(this)
        ApplicationContext = this
        binding = ActivityMainBinding.inflate(layoutInflater)
        vModelDownLoad = ViewModelDownLoad()
        val view = binding.root
        setContentView(view)
        setUpUi()
    }

    fun setUpUi(){

        /**
         * set up viewppage2
         */
        viewPage = AdapterMainActiivity(this)
        binding.contentView.adapter = viewPage
        // bottom
        setUpBottomNavigation()
        // viewpage
        setUpViewPage()
        // navigation drawer
        setUpNavigationDrawer()
    }

    /**
     * lắng nghe sự even viewpage mở fragment
     */
    private fun showFragment(fragmentCode: Int) {

        binding.contentView.setCurrentItem(fragmentCode)
        CoroutineScope(Dispatchers.IO).launch {
            delay(300)
            binding.mainActicity.closeDrawers()
        }

    }


    fun setUpBottomNavigation(){
        showFragment(ConstantDownLoadApp.codeFragmentDownLoad)
        binding.bottomMain.setOnItemSelectedListener{
            when(it.itemId){
                R.id.Process->{
                    showFragment(ConstantDownLoadApp.codeFragmentProcess)
                    codeCurrentFragment = ConstantDownLoadApp.codeFragmentProcess
                }
                R.id.Home ->{
                    showFragment(ConstantDownLoadApp.codeFragmentDownLoad)
                    codeCurrentFragment = ConstantDownLoadApp.codeFragmentDownLoad
                }
                R.id.History->{
                    showFragment(ConstantDownLoadApp.codeFragmentShowStorage)
                    codeCurrentFragment = ConstantDownLoadApp.codeFragmentShowStorage
                }
//                else->{
//                    showFragment(ConstantDownLoadApp.codeFragmentDownLoad)
//                    codeCurrentFragment = ConstantDownLoadApp.codeFragmentDownLoad
//                }
            }
            true
        }
    }

     /**
     * xử lý sự kiện viewpageer lắng nghe fragment thay đổi để điều chỉnh bottom and navigation Drawer
      */

    fun setUpViewPage(){
        // lắng nghe sự thay đổi khi add fragment vào
        binding.contentView.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when(position){
                    ConstantDownLoadApp.codeFragmentDownLoad->{
                        codeCurrentFragment = ConstantDownLoadApp.codeFragmentDownLoad
                        binding.bottomMain.menu.findItem(R.id.Home).isChecked = true
                        binding.navigationView.menu.findItem(R.id.History).isChecked = false
                        binding.navigationView.menu.findItem(R.id.Process).isChecked = false
                    }
                    ConstantDownLoadApp.codeFragmentProcess->{
                        codeCurrentFragment = ConstantDownLoadApp.codeFragmentProcess
                        binding.bottomMain.menu.findItem(R.id.Process).isChecked = true
                        binding.navigationView.menu.findItem(R.id.Home).isChecked = false
                        binding.navigationView.menu.findItem(R.id.History).isChecked = false
                    }
                    ConstantDownLoadApp.codeFragmentShowStorage->{
                        codeCurrentFragment = ConstantDownLoadApp.codeFragmentShowStorage
                        binding.bottomMain.menu.findItem(R.id.History).isChecked = true
                        binding.navigationView.menu.findItem(R.id.Home).isChecked = false
                        binding.navigationView.menu.findItem(R.id.Process).isChecked = false
                    }
                }
            }
        })
    }

    /**
     * xử lý sự kiện navigation drawer
     */
    fun setUpNavigationDrawer(){

        binding.toolbar.title = "DownLoad app"
        setSupportActionBar(binding.toolbar)
        toggle = ActionBarDrawerToggle(this,binding.mainActicity,binding.toolbar,R.string.opent_navigation,R.string.close_navigation )
        binding.mainActicity.addDrawerListener(toggle)
        binding.navigationView.setNavigationItemSelectedListener {
            it.isChecked = true
            when(it.itemId){
                R.id.Process->{
                    showFragment(ConstantDownLoadApp.codeFragmentProcess)
                }
                R.id.Home->{
                    showFragment(ConstantDownLoadApp.codeFragmentDownLoad)
                }
                R.id.History->{
                    showFragment(ConstantDownLoadApp.codeFragmentShowStorage)
                }
            }
            true
        }
        toggle.syncState()

    }

    /**
     * 3 override chức năng của navigation drawer (UI)
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            Log.d("main","true")
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

}