package com.muicvtools.mutils


import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat



class CustomProgressDialog {

    private var dialog: CustomDialog? = null

    fun show(context: Activity, theme: String, title: CharSequence?): Dialog {

        dismiss()

        val inflater = context.layoutInflater
        val root = inflater.inflate(R.layout.progress_dialog_view, null)
        val tvTitle: TextView = root.findViewById(R.id.tv_title)
        val cardView: CardView = root.findViewById(R.id.cardView)
        val progressBar: ProgressBar = root.findViewById(R.id.progressBar)

        title?.let { tvTitle.text = title }


        if (theme == "black") {
            cardView.setCardBackgroundColor(Color.BLACK)
            tvTitle.setTextColor(Color.WHITE)
        } else {
            cardView.setCardBackgroundColor(Color.WHITE)
            tvTitle.setTextColor(Color.BLACK)
        }


        // Progress Bar Color
        setColorFilter(
            progressBar.indeterminateDrawable,
            ResourcesCompat.getColor(context.resources, R.color.red_500, null)
        )

        dialog = CustomDialog(context)
        dialog?.setContentView(root)
        dialog?.show()
        return dialog!!
    }

    fun isShowing(): Boolean {
        return if (dialog != null) {
            dialog!!.isShowing
        } else false
    }


    fun dismiss() {
        dialog?.dismiss()
    }

    private fun setColorFilter(drawable: Drawable, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
        } else {
            @Suppress("DEPRECATION")
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }

    class CustomDialog(context: Context) : Dialog(context, R.style.CustomDialogTheme) {
        init {
            setCanceledOnTouchOutside(false)
            setCancelable(false)
            // Set Semi-Transparent Color for Dialog Background
            window?.decorView?.rootView?.setBackgroundResource(R.color.dialogBackground)
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                window?.decorView?.setOnApplyWindowInsetsListener { _, insets ->
                    insets.consumeSystemWindowInsets()
                }
            }
        }
    }
}