package com.p2p.wowlet.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.DialogSuccessBinding

class SuccessDialog(val value: String) : DialogFragment() {

    companion object {
        const val TAG_SUCCESS_DIALOG = "SuccessDialog"
        fun newInstance(value: String): SuccessDialog {
            return SuccessDialog(value)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: DialogSuccessBinding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_success, container, false
        )
        binding.vSuccessInfo.text = value
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            window?.setLayout(
                resources.getDimensionPixelSize(R.dimen.dp_228),
                resources.getDimensionPixelSize(R.dimen.dp_194)
            )
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

}