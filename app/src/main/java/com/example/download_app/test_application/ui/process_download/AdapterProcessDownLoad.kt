package com.example.download_app.test_application.ui.process_download

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.download_app.ConstantDownLoadApp
import com.example.download_app.R
import com.example.download_app.test_application.model.DownLoadProcess
import com.example.download_app.test_application.viewmodel.ViewModelDownLoad


class AdapterProcessDownLoad(val viewModel: ViewModelDownLoad) :
    RecyclerView.Adapter<AdapterProcessDownLoad.ViewHolder>() {

    var oldDataProcess = mutableListOf<DownLoadProcess>()


    class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val statusDownLoad: TextView
        var percentDownloadProgess: ProgressBar
        val control: ImageButton
        val percentDownLoad : TextView
        val totalSizeFile : TextView

        init {
            statusDownLoad = itemView.findViewById(R.id.item_Status)
            percentDownloadProgess = itemView.findViewById(R.id.progressBar)
            control = itemView.findViewById(R.id.item_control)
            percentDownLoad = itemView.findViewById(R.id.item_PercentDownLoad)
            totalSizeFile = itemView.findViewById(R.id.item_size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.items_mission_process, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.statusDownLoad.text = oldDataProcess[position].status
        holder.percentDownLoad.text = oldDataProcess[position].intPercent.toString()
        holder.percentDownloadProgess.progress = oldDataProcess[position].intPercent
        holder.totalSizeFile.text = oldDataProcess[position].totalSizeFile
        // set event click

    }

    override fun getItemCount(): Int {
        return oldDataProcess.size
    }


}