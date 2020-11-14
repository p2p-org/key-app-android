package com.p2p.wowlet.fragment.notification.dialog


import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.p2p.wowlet.R
import kotlinx.android.synthetic.main.dialog_fragment_enable_notification.*


class EnableNotificationDialog(private val enableData: () -> Unit) : DialogFragment() {
    companion object {
        const val TAG_ENABLE_NOTIFICATION_DIALOG = "EnableNotificationDialog"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_fragment_enable_notification, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btCancel.setOnClickListener {
            dismiss()
        }

        btEnable.setOnClickListener {
            enableData.invoke()
            dismiss()
        }
        dialog?.setCancelable(false)
        dialog?. window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}