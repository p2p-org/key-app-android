package com.p2p.wowlet.dashboard.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.DialogChangePhoneEmailBinding
import com.p2p.wowlet.dashboard.model.MailPhoneType
import com.p2p.wowlet.utils.viewbinding.viewBinding
import kotlinx.android.synthetic.main.dialog_change_phone_email.icClose

class ChangeMailPhoneDialog(val mailPhoneType: MailPhoneType) : DialogFragment() {

    companion object {
        const val TAG_CURRENCY_DIALOG = "ChangeMailPhoneDialog"
        fun newInstance(mailPhoneType: MailPhoneType): ChangeMailPhoneDialog {
            return ChangeMailPhoneDialog(mailPhoneType)
        }
    }

    private val binding: DialogChangePhoneEmailBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_change_phone_email, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        icClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            window?.setLayout(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isCancelable = false
        }
    }
}