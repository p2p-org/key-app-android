package org.p2p.wallet.settings.ui.recovery

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentRecoveryKitBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class RecoveryKitFragment :
    BaseMvpFragment<RecoveryKitContract.View, RecoveryKitContract.Presenter>(R.layout.fragment_recovery_kit),
    RecoveryKitContract.View {

    companion object {
        fun create(): RecoveryKitFragment = RecoveryKitFragment()
    }

    override val navBarColor: Int
        get() = R.color.night

    private val binding: FragmentRecoveryKitBinding by viewBinding()

    override val presenter: RecoveryKitContract.Presenter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            imageViewHelp.setOnClickListener {
                IntercomService.showMessenger()
            }
        }
    }

    override fun showDeviceData(device: String) = with(binding) {
        recoveryViewDevice.subtitle = device
    }

    override fun showPhoneData(phone: String) = with(binding) {
        recoveryViewPhone.subtitle = phone
    }

    override fun showSocialData(social: String) = with(binding) {
        recoveryViewSocial.subtitle = social
    }
}
