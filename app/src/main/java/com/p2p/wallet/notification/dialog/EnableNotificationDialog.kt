package com.p2p.wallet.notification.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.p2p.wallet.R
import com.p2p.wallet.databinding.DialogFragmentEnableNotificationBinding
import com.p2p.wallet.utils.viewbinding.viewBinding

class EnableNotificationDialog(private val enableData: () -> Unit) : DialogFragment() {
    companion object {
        const val TAG_ENABLE_NOTIFICATION_DIALOG = "EnableNotificationDialog"
    }

    private val binding: DialogFragmentEnableNotificationBinding by viewBinding()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_fragment_enable_notification, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btCancel.setOnClickListener {
            dismiss()
        }

        binding.btEnable.setOnClickListener {
            enableData.invoke()
            dismiss()
        }
        dialog?.setCancelable(false)
        dialog?. window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}