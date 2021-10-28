package org.p2p.wallet.settings.ui.settings

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.onboarding.OnboardingFragment
import org.p2p.wallet.auth.ui.username.ReservingUsernameFragment
import org.p2p.wallet.auth.ui.username.UsernameFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSettingsBinding
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
        val TAG: String = SettingsFragment::class.java.simpleName
    }

    override val presenter: SettingsContract.Presenter by inject()

    private val binding: FragmentSettingsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }
            securityTextView.setOnClickListener {
                replaceFragment(SecurityFragment.create())
            }

            zeroBalanceView.setOnClickListener {
                presenter.setZeroBalanceHidden(!hideZeroSwitch.isChecked)
            }

            networkTextView.setOnClickListener {
                replaceFragment(NetworkFragment.create())
            }

            appearanceTextView.setOnClickListener {
                replaceFragment(AppearanceFragment.create())
            }

            binding.usernameView.setOnClickListener {
                presenter.onUsernameClicked()
            }

            logoutView.clipToOutline = true
            logoutView.setOnClickListener {
                presenter.logout()
            }

            versionTextView.text = BuildConfig.VERSION_NAME
        }

        presenter.loadData()
    }

    override fun showHiddenBalance(isHidden: Boolean) {
        binding.hideZeroSwitch.isChecked = isHidden
    }

    override fun showAuthorization() {
        popAndReplaceFragment(
            target = OnboardingFragment.create(),
            inclusive = true
        )
    }

    override fun showUsername(username: String?) {
        binding.usernameValueTextView.text = username
    }

    override fun openUsernameScreen() {
        replaceFragment(UsernameFragment.create())
    }

    override fun openReserveUsernameScreen() {
        replaceFragment(ReservingUsernameFragment.create(TAG))
    }
}