package com.p2p.wowlet.fragment.dashboard.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.DialogChangePhoneEmailBinding
import com.p2p.wowlet.databinding.DialogCurrencyBinding
import com.p2p.wowlet.databinding.DialogProfileDetailsBinding
import com.wowlet.entities.enums.MailPhoneType

class ChangeMailPhoneDialog(val mailPhoneType: MailPhoneType) : DialogFragment() {

    companion object {
        const val TAG_CURRENCY_DIALOG = "ChangeMailPhoneDialog"
        fun newInstance(mailPhoneType: MailPhoneType): ChangeMailPhoneDialog {
            return ChangeMailPhoneDialog(mailPhoneType)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: DialogChangePhoneEmailBinding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_change_phone_email, container, false
        )
        binding.dialogType = mailPhoneType
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mailPhoneType
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            window?.setLayout(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

}