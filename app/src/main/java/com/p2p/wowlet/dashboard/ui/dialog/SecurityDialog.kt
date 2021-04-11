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
import com.p2p.wowlet.dashboard.ui.dialog.profile.viewmodel.ProfileViewModel
import com.p2p.wowlet.databinding.DialogSecurityBinding
import com.p2p.wowlet.utils.openFingerprintDialog
import com.p2p.wowlet.utils.viewbinding.viewBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SecurityDialog(
    private val onFingerprintStateSelected: () -> Unit
) : DialogFragment() {

    private val binding: DialogSecurityBinding by viewBinding()

    private val profileViewModel: ProfileViewModel by viewModel()

    companion object {

        const val TAG_SECURITY_DIALOG = "SecurityDialog"
        fun newInstance(onFingerprintStateSelected: () -> Unit): SecurityDialog {
            return SecurityDialog(onFingerprintStateSelected)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_security, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vClose.setOnClickListener {
            dismiss()
        }
        binding.vDone.setOnClickListener {
            val isFingerprintChecked = binding.vSwitch.isChecked
            fingerprintEnableState(isFingerprintChecked)
            dismiss()
        }
        binding.vSwitch.setOnClickListener {
            val isChecked = binding.vSwitch.isChecked
            if (isChecked) {
                binding.vSwitch.isChecked = false
                activity?.openFingerprintDialog {
                    binding.vSwitch.isChecked = it
                }
            }
        }

        binding.vSwitch.isChecked = profileViewModel.isUsesFingerprint().isEnable
    }

    private fun fingerprintEnableState(isFingerprintChecked: Boolean) {
        profileViewModel.setUsesFingerPrint(
            isEnabled = isFingerprintChecked,
            isNotWantEnable = false
        ).invokeOnCompletion {
            MainScope().launch { onFingerprintStateSelected.invoke() }
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            window?.setLayout(width, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isCancelable = false
        }
    }
}