package com.example.download_app.test_application.ui.adapter

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.download_app.R
import com.example.download_app.test_application.model.DownloadAdapter
import com.example.download_app.test_application.viewmodel.ViewModelDownLoad

class AdapterFragmentDownLoad(val data : DownloadAdapter, val viewModel : ViewModelDownLoad) : RecyclerView.Adapter<AdapterFragmentDownLoad.ViewHolder>()  {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            val thumbal : ImageView
            val title : TextView
            val chooseQuality : TextView
            val downloadButton : Button
        init {
            thumbal = itemView.findViewById(R.id.thumball)
            title = itemView.findViewById(R.id.title)
            chooseQuality = itemView.findViewById(R.id.quality)
            downloadButton = itemView.findViewById(R.id.download)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_download_fragment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = data.titleFile
        Glide.with(holder.itemView.context).load(
            data.imageThumb
        ).into(holder.thumbal)
        holder.chooseQuality.text = data.downloadQuality?.get(position)?.quality.toString()
        holder.downloadButton.setOnClickListener {
            viewModel.resourceUrlYoutube.postValue(data.downloadQuality?.get(position)?.url_stream)
        }
    }

    override fun getItemCount(): Int {
        return data.numberItems
    }
}