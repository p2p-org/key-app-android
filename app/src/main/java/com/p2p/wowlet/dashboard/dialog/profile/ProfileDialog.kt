package com.p2p.wowlet.dashboard.dialog.profile

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.dashboard.dialog.networks.viewmodel.NetworkViewModel
import com.p2p.wowlet.dashboard.dialog.profile.viewmodel.ProfileViewModel
import com.p2p.wowlet.databinding.DialogProfileBinding
import com.p2p.wowlet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileDialog(
    private val openProfileDetail: () -> Unit,
    private val openBackup: () -> Unit,
    private val openNetwork: (onNetworkSelected: () -> Unit) -> Unit,
    private val openCurrency: (onCurrencySelected: () -> Unit) -> Unit,
    private val openSavedCard: () -> Unit,
    private val openSecurity: (onFingerprintStateSelected: () -> Unit) -> Unit,

) : DialogFragment() {

    companion object {

        const val TAG_PROFILE_DIALOG = "ProfileDialog"
        fun newInstance(
            openProfileDetail: () -> Unit,
            openBackup: () -> Unit,
            openNetwork: (onNetworkSelected: () -> Unit) -> Unit,
            openCurrency: (onCurrencySelected: () -> Unit) -> Unit,
            openSavedCard: () -> Unit,
            openSecurity: (onFingerprintStateSelected: () -> Unit) -> Unit,
        ): ProfileDialog {
            return ProfileDialog(
                openProfileDetail,
                openBackup,
                openNetwork,
                openCurrency,
                openSavedCard,
                openSecurity
            )
        }
    }

    private val profileViewModel: ProfileViewModel by viewModel()
    private val networkViewModel: NetworkViewModel by viewModel()

    private val binding: DialogProfileBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            txtCurrencyName.text = profileViewModel.getSelectedCurrencyName()
            txtSelectedNetworkName.text = networkViewModel.getSelectedNetworkName()

            val isEnabled = profileViewModel.isUsesFingerprint().isEnable
            txtSecurityOptions.text = getString(if (isEnabled) (R.string.fingerprint_and_pin) else R.string.pin)

            vDone.setOnClickListener {
                dismiss()
            }
            lUserInfoContainer.setOnClickListener {
                openProfileDetail.invoke()
            }
            lBackupContainer.setOnClickListener {
                openBackup.invoke()
            }
            lNetworkContainer.setOnClickListener {
                openNetwork.invoke(onNetworkSelected)
            }
            lCurrencyContainer.setOnClickListener {
                openCurrency.invoke(onCurrencySelected)
            }
//        lSavedCardsContainer.setOnClickListener {
//            openSavedCard.invoke()
//        }
            lSecurityContainer.setOnClickListener {
                openSecurity.invoke(onFingerprintStateSelected)
            }
        }
    }

    private val onCurrencySelected: () -> Unit = {
        binding.txtCurrencyName.text = profileViewModel.getSelectedCurrencyName()
    }

    private val onNetworkSelected: () -> Unit = {
        binding.txtSelectedNetworkName.text = networkViewModel.getSelectedNetworkName()
    }

    private val onFingerprintStateSelected: () -> Unit = {
        binding.txtSecurityOptions.text =
            if (profileViewModel.isUsesFingerprint().isEnable) {
                getString(R.string.fingerprint_and_pin)
            } else {
                getString(R.string.pin)
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