package com.example.download_app.ui.process_download

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
import com.example.ConstantDownLoadApp
import com.example.download_app.R
import com.example.download_app.model.ProcessData
import com.example.download_app.viewmodel.ViewModelDownLoad


class AdapterProcessDownLoad(val viewModel: ViewModelDownLoad) :
    RecyclerView.Adapter<AdapterProcessDownLoad.ViewHolder>() {

    var oldDataProcess = mutableListOf<ProcessData>()


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
        val data = oldDataProcess[position]
        val downloadManager = holder.itemView.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        holder.statusDownLoad.text = oldDataProcess[position].status
        holder.percentDownLoad.text = oldDataProcess[position].intPercent.toString()
        holder.percentDownloadProgess.progress = oldDataProcess[position].intPercent
        holder.totalSizeFile.text = oldDataProcess[position].totalSizeFile
        // set event click

        holder.control.setOnClickListener {
            when (viewModel.dataProcessDownLoad[position].conditionControl) {

                /**
                 *  tien trinh danng pause set event resume (conditionControl là biến nhận định pause or resume)
                  */
                ConstantDownLoadApp.actionPause -> {
                    viewModel.dataProcessDownLoad[position].conditionControl = ConstantDownLoadApp.actionResume
                    // tiep tuc tiến trình download
                    resumeDownload(
                        holder.itemView.context,
                        oldDataProcess[position].id_ProcessDownLoad
                    )
                    if (!resumeDownload(
                            holder.itemView.context,
                            oldDataProcess[position].id_ProcessDownLoad
                        )
                    ) {
                        Toast.makeText(
                            holder.itemView.context,
                            "Failed to Pause",
                            Toast.LENGTH_SHORT
                        ).show()
                    }else{
                        viewModel.getMessageProcessOrStatus(data.id_ProcessDownLoad,downloadManager,oldDataProcess,position)
                    }

                    Log.d("adapter","resume")
                }
                // tien trinh danng o trang thai chay va set event pause
                ConstantDownLoadApp.actionResume -> {
                    viewModel.dataProcessDownLoad[position].conditionControl = ConstantDownLoadApp.actionPause
                    pauseDownload(
                        holder.itemView.context,
                        oldDataProcess[position].id_ProcessDownLoad
                    )
                    if (!pauseDownload(
                            holder.itemView.context,
                            oldDataProcess[position].id_ProcessDownLoad
                        )
                    ) {
                        Toast.makeText(
                            holder.itemView.context,
                            "Failed to Resume",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Log.d("adapter","pause")
                }
                ConstantDownLoadApp.actionDownLoadComplete -> {
                    Toast.makeText(holder.itemView.context, "SUCCESS_DOWWNLOAD", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        if (viewModel.dataProcessDownLoad[position].conditionControl==ConstantDownLoadApp.actionPause){
            holder.control.setImageResource(R.drawable.ic_baseline_play_circle_24)
        }
        else{
            holder.control.setImageResource(R.drawable.ic_baseline_pause_circle_24)
        }
    }

    override fun getItemCount(): Int {
        return oldDataProcess.size
    }

    /**
     * tiến trình tiếp tục download
     */

    fun resumeDownload(context: Context, idProcessdownload: Long): Boolean {
        var updatedRow = 0
        val resumeDownload = ContentValues()
        resumeDownload.put("control", 0) // Resume Control Value
        try {
            updatedRow = context
                .contentResolver
                .update(
                    Uri.parse("content://downloads/my_downloads"),
                    resumeDownload,
                    "${MediaStore.Audio.Media._ID} = ?",
                    arrayOf(idProcessdownload.toString())
                )
        } catch (e: Exception) {
            Log.e("ViewModelDownLoad", "Failed to update control for downloading video")
        }
        return 0 < updatedRow
    }


    /**
     * tiến trình tạm dừng donwnload
     */
    fun pauseDownload(context: Context, idProcessdownload: Long): Boolean {
        var updatedRow = 0
        val pauseDownload = ContentValues()
        pauseDownload.put("control", 1) // Pause Control Value
        try {
            updatedRow = context
                .contentResolver
                .update(
                    Uri.parse("content://downloads/my_downloads"),
                    pauseDownload,
                    "${MediaStore.Audio.Media._ID}=?",
                    arrayOf(idProcessdownload.toString())
                )
        } catch (e: java.lang.Exception) {
            Log.e("ViewModel", "Failed to update control for downloading video")
        }

        return 0 < updatedRow
    }


}