package com.muicvtools.mutils

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.muicvtools.mutils.databinding.ActivityNotificationBinding


class NotificationActivity : AdOverlayActivity() {
    private var viewModel: NotifyViewModel? = null
    private lateinit var binding: ActivityNotificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
//        toolbar.title = "Notifications"
//        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_white_24dp)
//        setSupportActionBar(toolbar)
        title = "Notifications"

        if (intent.getStringExtra("notify") == "disable") {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        } else
            supportActionBar?.setDisplayHomeAsUpEnabled(true)



        viewModel = ViewModelProvider(this).get(NotifyViewModel::class.java)

        val adapter = NotificationAdapter()

        viewModel?.linkList?.observe(this, Observer {
            if (it.isNullOrEmpty()) {
                Toast.makeText(this, "No records found", Toast.LENGTH_LONG).show()
            } else {
                adapter.setData(it)
            }
        })

        Log.d("adsdk","aaaaa");

        ApiManager.getSharedInstance().getNotify(this, object : NotifyListener {
            override fun getNotifySuccess(result: NotifyResult) {
                runOnUiThread { viewModel?.setData(result.notices) }
            }

            override fun getNotifyFail() {
                Log.d("", "")
            }
        })
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    override fun onBackPressed() {
        Log.d("adsdk", "d= "+intent.getStringExtra("notify"))
        if (intent.getStringExtra("notify") == "disable") {
            return
        } else
            super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return  true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun isShowOpenAds(): Boolean {
        return true
    }
}