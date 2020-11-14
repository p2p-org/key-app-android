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
import com.p2p.wowlet.databinding.DialogCurrencyBinding
import com.p2p.wowlet.databinding.DialogNetworksBinding
import com.p2p.wowlet.databinding.DialogProfileDetailsBinding

class NetworksDialog : DialogFragment() {

    companion object {
        const val TAG_NETWORKS_DIALOG = "NetworksDialog"
        fun newInstance(): NetworksDialog {
            return NetworksDialog()
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: DialogNetworksBinding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_networks, container, false
        )
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            window?.setLayout(width, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

}