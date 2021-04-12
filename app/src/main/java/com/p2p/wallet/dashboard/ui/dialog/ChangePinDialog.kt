package com.p2p.wallet.dashboard.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.p2p.wallet.R
import com.p2p.wallet.databinding.DialogChangePinBinding
import com.p2p.wallet.utils.viewbinding.viewBinding

class ChangePinDialog() : DialogFragment() {

    companion object {
        const val TAG_CHANGE_PIN_DIALOG = "ChangePinDialog"
        fun newInstance(): ChangePinDialog {
            return ChangePinDialog()
        }
    }

    private val binding: DialogChangePinBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_change_pin, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.icClose.setOnClickListener {
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