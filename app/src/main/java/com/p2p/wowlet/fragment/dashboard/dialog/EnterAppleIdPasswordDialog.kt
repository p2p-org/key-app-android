package com.p2p.wowlet.fragment.dashboard.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.DialogEnterAppleIdPassBinding
import com.p2p.wowlet.databinding.DialogSuccessBinding

class EnterAppleIdPasswordDialog( ) : DialogFragment() {

    companion object {
        const val TAG_APPLE_ID_DIALOG = "EnterAppleIdPasswordDialog"
        fun newInstance( ): EnterAppleIdPasswordDialog {
            return EnterAppleIdPasswordDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: DialogEnterAppleIdPassBinding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_enter_apple_id_pass, container, false
        )
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            window?.setLayout(
                resources.getDimensionPixelSize(R.dimen.dp_270),
                resources.getDimensionPixelSize(R.dimen.dp_178)
            )
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

}