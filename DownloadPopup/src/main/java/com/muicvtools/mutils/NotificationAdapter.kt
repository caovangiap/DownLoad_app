package com.muicvtools.mutils

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*


class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.NotifyViewHolder>() {
    private var mutableList = mutableListOf<Notify>()

    fun setData(list: List<Notify>) {
        mutableList.clear()
        mutableList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifyViewHolder {
        return NotifyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false))
    }

    override fun getItemCount(): Int {
        return mutableList.size
    }

    override fun onBindViewHolder(holder: NotifyViewHolder, position: Int) {
        val item = mutableList[position]
        val time = SimpleDateFormat("HH:mm   MM/dd/yyyy", Locale.getDefault()).format(item.time.getDate())

        holder.tvTitle.text = item.title
        holder.tvMessage.text = item.msg
        holder.tvDate.text = time

        if (item.url.isNullOrEmpty()) {
            holder.tvSeemore.visibility = View.GONE
        } else {
            holder.tvSeemore.visibility = View.VISIBLE
            holder.tvSeemore.paintFlags = holder.tvSeemore.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            holder.tvSeemore.setOnClickListener { it.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.url))) }
        }

    }

    class NotifyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvMessage: TextView = itemView.findViewById(R.id.tv_msg)
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvSeemore: TextView = itemView.findViewById(R.id.tv_seemore)
    }

}