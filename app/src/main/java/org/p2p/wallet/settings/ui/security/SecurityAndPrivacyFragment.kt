package org.p2p.wallet.settings.ui.security

import androidx.core.view.isVisible
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.utils.insets.appleBottomInsets
import org.p2p.core.utils.insets.appleTopInsets
import org.p2p.core.utils.insets.consume
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSecurityAndPrivacyBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.settings.ui.devices.DevicesFragment
import org.p2p.wallet.settings.ui.security.unlock.SeedPhraseUnlockFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.showInfoDialog
import org.p2p.wallet.utils.viewbinding.viewBinding

class SecurityAndPrivacyFragment :
    BaseMvpFragment<SecurityAndPrivacyContract.View, SecurityAndPrivacyContract.Presenter>(
        R.layout.fragment_security_and_privacy
    ),
    SecurityAndPrivacyContract.View {

    companion object {
        fun create(): SecurityAndPrivacyFragment = SecurityAndPrivacyFragment()
    }

    private val binding: FragmentSecurityAndPrivacyBinding by viewBinding()

    override val presenter: SecurityAndPrivacyContract.Presenter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            imageViewHelp.setOnClickListener {
                IntercomService.showMessenger()
            }
            recoveryViewSeed.setOnClickListener {
                presenter.onSeedPhraseClicked()
            }

            textViewDeviceManage.setOnClickListener {
                replaceFragment(DevicesFragment.create())
            }
        }
    }

    override fun applyWindowInsets(rootView: View) {
        rootView.doOnApplyWindowInsets { _, insets, _ ->
            insets.systemAndIme().consume {
                binding.toolbar.appleTopInsets(this)
                rootView.appleBottomInsets(this)
            }
        }
    }

    override fun showDeviceName(deviceName: CharSequence, showWarning: Boolean) {
        binding.recoveryViewDevice.apply {
            subtitle = deviceName
            if (showWarning) {
                setSubtitleDrawable(left = R.drawable.ic_warning_solid)
                setSubtitleColor(getColor(R.color.icons_rose))
            }
        }
    }

    override fun showManageVisible(isVisible: Boolean) {
        binding.textViewDeviceManage.isVisible = isVisible
    }

    override fun showPhoneNumber(phoneNumber: String) {
        binding.recoveryViewPhone.subtitle = phoneNumber
    }

    override fun showSocialId(socialId: String) {
        binding.recoveryViewSocial.subtitle = socialId
    }

    override fun setWebAuthInfoVisibility(isVisible: Boolean) = with(binding) {
        textViewRecoverySubText.setText(R.string.recovery_sub_text_web3)
        layoutWebAuthInfo.isVisible = isVisible
        recoveryViewSeed.setBackgroundColor(getColor(R.color.bg_snow))
    }

    override fun showLogoutInfoDialog() {
        showInfoDialog(
            titleRes = R.string.recovery_kit_logout_title,
            messageRes = R.string.recovery_kit_logout_message,
            primaryButtonRes = R.string.common_logout,
            primaryCallback = { presenter.logout() },
            secondaryButtonRes = R.string.common_stay,
            primaryButtonTextColor = R.color.systemErrorMain
        )
    }

    override fun showSeedPhraseLockFragment() {
        replaceFragment(SeedPhraseUnlockFragment.create())
    }
}
