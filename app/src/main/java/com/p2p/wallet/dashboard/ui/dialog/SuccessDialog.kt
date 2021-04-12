package com.p2p.wallet.dashboard.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.p2p.wallet.R
import com.p2p.wallet.databinding.DialogSuccessBinding
import com.p2p.wallet.utils.viewbinding.viewBinding

class SuccessDialog(val value: String) : DialogFragment() {

    companion object {
        const val TAG_SUCCESS_DIALOG = "SuccessDialog"
        fun newInstance(value: String): SuccessDialog {
            return SuccessDialog(value)
        }
    }

    private val binding: DialogSuccessBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_success, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vSuccessInfo.text = value
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