package com.p2p.wallet.settings.ui.settings

import android.os.Bundle
import android.view.View
import com.p2p.wallet.BuildConfig
import com.p2p.wallet.R
import com.p2p.wallet.auth.ui.onboarding.OnboardingFragment
import com.p2p.wallet.auth.ui.username.ReservingUsernameFragment
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.databinding.FragmentSettingsBinding
import com.p2p.wallet.settings.ui.appearance.AppearanceFragment
import com.p2p.wallet.settings.ui.network.NetworkFragment
import com.p2p.wallet.settings.ui.security.SecurityFragment
import com.p2p.wallet.utils.popAndReplaceFragment
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.replaceFragment
import com.p2p.wallet.utils.toast
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject

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
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }
            securityTextView.setOnClickListener {
                replaceFragment(SecurityFragment.create())
            }

            zeroBalanceView.setOnClickListener {
                presenter.setZeroBalanceHidden(!hideZeroSwitch.isChecked)
            }

            usernameView.setOnClickListener {
                if (presenter.checkUsername()) {
                    replaceFragment(ReservingUsernameFragment.create())
                    toast(text = "exist")
                } else {
                    replaceFragment(ReservingUsernameFragment.create())
                    toast(text = "not exist")
                }
            }

            networkTextView.setOnClickListener {
                replaceFragment(NetworkFragment.create())
            }

            appearanceTextView.setOnClickListener {
                replaceFragment(AppearanceFragment.create())
            }

            logoutTextView.setOnClickListener {
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
}