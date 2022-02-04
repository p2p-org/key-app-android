package org.p2p.wallet.settings.ui.settings

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.ReserveMode
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.auth.ui.onboarding.OnboardingFragment
import org.p2p.wallet.auth.ui.username.ReserveUsernameFragment
import org.p2p.wallet.auth.ui.username.UsernameFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSettingsBinding
import org.p2p.wallet.settings.model.SettingItem
import org.p2p.wallet.settings.ui.appearance.AppearanceFragment
import org.p2p.wallet.settings.ui.network.NetworkFragment
import org.p2p.wallet.settings.ui.security.SecurityFragment
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class SettingsFragment :
    BaseMvpFragment<SettingsContract.View, SettingsContract.Presenter>(R.layout.fragment_settings),
    SettingsContract.View {

    companion object {
        fun create() = SettingsFragment()
    }

    override val presenter: SettingsContract.Presenter by inject()

    private val binding: FragmentSettingsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
        }

        presenter.loadData()
    }

    override fun showProfile(items: List<SettingItem>) {
        binding.profileSettings.setup(items)
    }

    override fun showNetwork(items: List<SettingItem>) {
    }

    override fun onProfileItemClicked(titleRes: Int) {
    }

    override fun onNetworkItemClicked(titleRes: Int) {
    }
}

interface FeeRelayerApi {
    interface DevApi: FeeRelayerApi {

    }

    interface Api: FeeRelayerApi {

    }
}