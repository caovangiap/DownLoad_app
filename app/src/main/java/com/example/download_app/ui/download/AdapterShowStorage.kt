package com.example.download_app.ui.download

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Size
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.download_app.R
import com.example.download_app.model.StorageData
import com.example.download_app.viewmodel.ViewModelDownLoad

/**
 * Các action thêm sửa xóa được tích hợp vào items view trong recycler
 */

class AdapterShowStorage(
    val dataVideo: MutableList<StorageData>,
    val ViewModel: ViewModelDownLoad,
    val fragment : FragmentShowStorage
) : RecyclerView.Adapter<AdapterShowStorage.ViewHolder>() {

    lateinit var ACTION_CODE : String
    val EDIT_CODE = "100"
    val REMOVE_CODE = "200"

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var videoThumb: ImageView
        var time: TextView
        var name: TextView
        var updateClick: Button

        init {
            videoThumb = itemView.findViewById(R.id.video)
            time = itemView.findViewById(R.id.Time)
            name = itemView.findViewById(R.id.Name)
            updateClick = itemView.findViewById(R.id.update)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.items_storage_videodownload, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        /**
         * Load ảnh thumb của video hiển thị
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val thumbnail: Bitmap =
                holder.itemView.context.contentResolver.loadThumbnail(
                    Uri.parse(dataVideo[position].Uri.toString()), Size(640, 480), null
                )

            Glide.with(holder.itemView.context)
                .load(thumbnail)
                .into(holder.videoThumb)
        } else {

            Glide.with(holder.itemView.context)
                .load(dataVideo[position].Uri)
                .into(holder.videoThumb)
        }
        holder.time.text = dataVideo[position].Dration.toString()
        holder.name.text = dataVideo[position].Name

        /**
         *  xét sự kiện cho các sự kiện items media
         */
        holder.updateClick.setOnClickListener {
            val popup = PopupMenu(holder.itemView.context, holder.updateClick)
            popup.inflate(R.menu.menu_option_editstorage)

            popup.setOnMenuItemClickListener { item: MenuItem? ->

                when (item!!.itemId) {
                    R.id.Edit -> {
                        editTextDialogforNameMedia(holder.itemView.context, position)
                    }
                    R.id.Remove -> {
                        optionRemove(position,holder.itemView.context)
                    }
                }

                true
            }

            popup.show()
        }


    }

    override fun getItemCount(): Int {
        return dataVideo.size
    }

    // change items
    fun itemsChange(position: Int, updateName: String) {
        dataVideo[position].Name = updateName
        notifyItemChanged(position)
    }
    // remove items
    fun removeItems(position: Int){
        dataVideo.removeAt(position)
        notifyItemRemoved(position)
    }


    /**
     * update sửa tên mediia trong external storage
     */
    fun editTextDialogforNameMedia(context: Context, position: Int) {

        // action code đẻ bên fragment intentSenderLaucher hoạt động đúng chức năng
        ACTION_CODE = EDIT_CODE
        val dialogLayout = LayoutInflater.from(context).inflate(R.layout.dialog_media, null)
        val inputTextChange = dialogLayout.findViewById<EditText>(R.id.inputMedia)
        AlertDialog.Builder(context)
            .setTitle(" Vui lòng nhập tên mới cho media")
            .setView(dialogLayout)
            .setPositiveButton("OK") { dialog, which ->
                if (inputTextChange.text.toString().isEmpty()) {
                    Toast.makeText(
                        context,
                        "Tên Media không được cập nhật khi để trống",
                        Toast.LENGTH_SHORT
                    )
                        .show()

                } else {

                    // cập nhật thông tin cho file external của app khác
                    fragment.updateItemsMediaRecycler(
                        dataVideo[position].id,
                        inputTextChange.text.toString(),
                        dataVideo[position].Uri,
                        position
                    )
                }
            }
            .show()


    }

    /**
     * sự kiện Remove items trong external storage
     */
    fun optionRemove(position: Int, context: Context) {
        ACTION_CODE = REMOVE_CODE
        AlertDialog.Builder(context)
            .setTitle(" Are You Sure")
            .setMessage("Bạn muốn xóa items này ?")
            .setPositiveButton("OK") { dialog, which ->
                    // cập nhật thông tin cho file external của app khác
                fragment.updateItemsMediaRecycler(
                    dataVideo[position].id,
                    "",
                    dataVideo[position].Uri,
                    position
                )
            }
            .setNegativeButton("Cancel"){ dialog,which ->
                dialog.dismiss()
            }
            .show()
    }
}