package org.p2p.wallet.settings.ui.recovery

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentRecoveryKitBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.settings.ui.recovery.unlock_seed_phrase.SeedPhraseUnlockFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.showInfoDialog
import org.p2p.wallet.utils.viewbinding.viewBinding

class RecoveryKitFragment :
    BaseMvpFragment<RecoveryKitContract.View, RecoveryKitContract.Presenter>(R.layout.fragment_recovery_kit),
    RecoveryKitContract.View {

    companion object {
        fun create(): RecoveryKitFragment = RecoveryKitFragment()
    }

    private val binding: FragmentRecoveryKitBinding by viewBinding()

    override val presenter: RecoveryKitContract.Presenter by inject()

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
        }
    }

    override fun showDeviceName(deviceName: String) {
        binding.recoveryViewDevice.subtitle = deviceName
    }

    override fun showPhoneNumber(phoneNumber: String) {
        binding.recoveryViewPhone.subtitle = phoneNumber
    }

    override fun showSocialId(socialId: String) {
        binding.recoveryViewSocial.subtitle = socialId
    }

    override fun setWebAuthInfoVisibility(isVisible: Boolean) {
        binding.layoutWebAuthInfo.isVisible = isVisible
        binding.recoveryViewSeed.setBackgroundColor(getColor(R.color.bg_snow))
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
