package org.p2p.wallet.settings.ui.settings

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.setFragmentResultListener
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.ReserveMode
import org.p2p.wallet.auth.ui.username.ReserveUsernameFragment
import org.p2p.wallet.auth.ui.username.UsernameFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSettingsBinding
import org.p2p.wallet.settings.model.SettingsRow
import org.p2p.wallet.settings.ui.network.SettingsNetworkFragment
import org.p2p.wallet.settings.ui.reset.ResetPinFragment
import org.p2p.wallet.settings.ui.security.SecurityFragment
import org.p2p.wallet.settings.ui.zerobalances.SettingsZeroBalanceFragment
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.showInfoDialog
import org.p2p.wallet.utils.viewbinding.viewBinding

class SettingsFragment :
    BaseMvpFragment<SettingsContract.View, SettingsContract.Presenter>(R.layout.fragment_settings),
    SettingsContract.View {

    companion object {
        fun create() = SettingsFragment()
    }

    override val presenter: SettingsContract.Presenter by inject()

    private val binding: FragmentSettingsBinding by viewBinding()
    private val adapter = SettingsAdapter(::onItemClickListener, ::onLogoutClickListener)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            recyclerView.attachAdapter(adapter)
        }
        setListeners()
        presenter.loadData()
    }

    private fun setListeners() {
        setFragmentResultListener(ReserveUsernameFragment.REQUEST_KEY) { key, bundle ->
            showUsername()
        }
        setFragmentResultListener(ResetPinFragment.REQUEST_KEY) { key, bundle ->
            val isPinChanged = bundle.getBoolean(ResetPinFragment.BUNDLE_IS_PIN_CHANGED_KEY)
            if (!isPinChanged) return@setFragmentResultListener
            showSnackbar(message = getString(R.string.settings_a_new_wallet_pin_is_set), iconRes = R.drawable.ic_done)
        }

        setFragmentResultListener(SettingsNetworkFragment.REQUEST_KEY) { key, bundle ->
            val isNetworkChanged = bundle.getBoolean(SettingsNetworkFragment.BUNDLE_KEY_IS_NETWORK_CHANGED)
            presenter.onNetworkChanged(isNetworkChanged)
        }
    }

    override fun showSettings(item: List<SettingsRow>) {
        adapter.setData(item)
    }

    override fun showReserveUsername() {
        replaceFragment(ReserveUsernameFragment.create(ReserveMode.POP))
    }

    override fun showUsername() {
        replaceFragment(UsernameFragment.create())
    }

    private fun onItemClickListener(@StringRes titleResId: Int) {
        when (titleResId) {
            R.string.settings_username -> presenter.onUsernameClicked()

            R.string.settings_wallet_pin -> {
                replaceFragment(ResetPinFragment.create())
            }
            R.string.settings_app_security -> {
                replaceFragment(SecurityFragment.create())
            }
            R.string.settings_network -> {
                addFragment(
                    SettingsNetworkFragment.create(),
                    enter = 0,
                    exit = 0,
                    popEnter = 0,
                    popExit = 0
                )
            }
            R.string.settings_zero_balances -> {
                addFragment(
                    SettingsZeroBalanceFragment.create(),
                    enter = 0,
                    exit = 0,
                    popEnter = 0,
                    popExit = 0
                )
            }
            R.string.settings_pay_fees_with -> {
            }
            R.string.settings_staying_up_in_date -> {
            }
            R.string.settings_default_currency -> {
            }
            R.string.settings_appearance -> {
            }
            R.string.settings_address_book -> {
            }
            R.string.settings_history -> {
            }
            R.string.settings_backup -> {
            }
            R.string.settings_app_version -> {
            }
        }
    }

    private fun onLogoutClickListener() {
        showInfoDialog(
            titleRes = R.string.settings_logout_title,
            messageRes = R.string.settings_logout_message,
            primaryButtonRes = R.string.common_logout,
            primaryCallback = { presenter.logout() },
            secondaryButtonRes = R.string.common_stay,
            secondaryCallback = { },
            primaryButtonTextColor = R.color.systemErrorMain
        )
    }
}