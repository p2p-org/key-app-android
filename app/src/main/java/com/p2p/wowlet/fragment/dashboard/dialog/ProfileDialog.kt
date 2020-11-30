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
import com.p2p.wowlet.databinding.DialogProfileBinding
import com.p2p.wowlet.fragment.dashboard.viewmodel.DashboardViewModel
import com.p2p.wowlet.fragment.sendcoins.viewmodel.SendCoinsViewModel
import kotlinx.android.synthetic.main.dialog_profile.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileDialog(private val openProfileDetail: () -> Unit) : DialogFragment() {
    companion object {

        const val TAG_PROFILE_DIALOG = "ProfileDialog"
        fun newInstance(openProfileDetail: () -> Unit): ProfileDialog {
            return ProfileDialog(openProfileDetail)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: DialogProfileBinding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_profile, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vDone.setOnClickListener {
            dismiss()
        }
        lUserInfoContainer.setOnClickListener{
            openProfileDetail.invoke()
        }
    }
    override fun onResume() {
        super.onResume()
        dialog?.run {
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            window?.setLayout(width, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isCancelable=false
        }
    }

}