package org.p2p.wallet.settings.ui.settings

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.ReserveMode
import org.p2p.wallet.auth.ui.username.ReserveUsernameFragment
import org.p2p.wallet.auth.ui.username.UsernameFragment
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.analytics.constants.ScreenNames
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

private const val REQUEST_KEY = "EXTRA_REQUEST_KEY"
private const val BUNDLE_KEY_NEW_NETWORK_NAME = "EXTRA_NETWORK_NAME"
private const val BUNDLE_KEY_IS_PIN_CHANGED = "EXTRA_IS_PIN_CHANGED"
private const val BUNDLE_KEY_IS_ZERO_BALANCE_VISIBLE = "EXTRA_IS_ZERO_BALANCE_VISIBLE"

class SettingsFragment :
    BaseMvpFragment<SettingsContract.View, SettingsContract.Presenter>(R.layout.fragment_settings),
    SettingsContract.View {

    companion object {

        fun create() = SettingsFragment()
    }

    override val presenter: SettingsContract.Presenter by inject()

    private val binding: FragmentSettingsBinding by viewBinding()
    private val adapter = SettingsAdapter(::onItemClickListener, ::onLogoutClickListener)
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            recyclerView.attachAdapter(adapter)
        }

        requireActivity().supportFragmentManager.setFragmentResultListener(
            REQUEST_KEY,
            viewLifecycleOwner
        ) { _, result ->
            when {
                result.containsKey(BUNDLE_KEY_IS_PIN_CHANGED) -> {
                    onPinChanged(result)
                }
                result.containsKey(BUNDLE_KEY_NEW_NETWORK_NAME) -> {
                    onNetworkChanged(result)
                }
                result.containsKey(BUNDLE_KEY_IS_ZERO_BALANCE_VISIBLE) -> {
                    onZeroBalanceSettingsChanged(result)
                }
            }
        }
        presenter.loadData()
    }

    override fun showSettings(item: List<SettingsRow>) {
        adapter.setData(item)
    }

    override fun showReserveUsername() {
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Settings.USERNAME_RESERVE)
        replaceFragment(ReserveUsernameFragment.create(mode = ReserveMode.POP, isSkipStepEnabled = false))
    }

    override fun showUsername() {
        replaceFragment(UsernameFragment.create())
    }

    override fun showLogoutConfirm() {
        showInfoDialog(
            titleRes = R.string.settings_logout_title,
            messageRes = R.string.settings_logout_message,
            primaryButtonRes = R.string.common_logout,
            primaryCallback = {
                presenter.logout()
            },
            secondaryButtonRes = R.string.common_stay,
            primaryButtonTextColor = R.color.systemErrorMain
        )
    }

    private fun onItemClickListener(@StringRes titleResId: Int) {
        when (titleResId) {
            R.string.settings_username -> presenter.onUsernameClicked()

            R.string.settings_wallet_pin -> {
                analyticsInteractor.logScreenOpenEvent(ScreenNames.Settings.PIN_RESET)
                replaceFragment(ResetPinFragment.create(REQUEST_KEY, BUNDLE_KEY_IS_PIN_CHANGED))
            }
            R.string.settings_app_security -> {
                analyticsInteractor.logScreenOpenEvent(ScreenNames.Settings.SECURITY)
                replaceFragment(SecurityFragment.create())
            }
            R.string.settings_network -> {
                analyticsInteractor.logScreenOpenEvent(ScreenNames.Settings.NETWORK)
                addFragment(
                    SettingsNetworkFragment.create(REQUEST_KEY, BUNDLE_KEY_NEW_NETWORK_NAME),
                    enter = 0,
                    exit = 0,
                    popEnter = 0,
                    popExit = 0
                )
            }
            R.string.settings_zero_balances -> {
                analyticsInteractor.logScreenOpenEvent(ScreenNames.Settings.ZERO_BALANCES)
                addFragment(
                    SettingsZeroBalanceFragment.create(REQUEST_KEY, BUNDLE_KEY_IS_ZERO_BALANCE_VISIBLE),
                    enter = 0,
                    exit = 0,
                    popEnter = 0,
                    popExit = 0
                )
            }
        }
    }

    private fun onLogoutClickListener() {
        presenter.onLogoutClicked()
    }

    private fun onPinChanged(bundle: Bundle) {
        val isPinChanged = bundle.getBoolean(BUNDLE_KEY_IS_PIN_CHANGED)
        if (isPinChanged) {
            showSuccessSnackBar(R.string.settings_a_new_wallet_pin_is_set)
        }
    }

    private fun onNetworkChanged(bundle: Bundle) {
        val networkName = bundle.getString(BUNDLE_KEY_NEW_NETWORK_NAME)
        if (!networkName.isNullOrEmpty()) {
            presenter.onNetworkChanged(newName = networkName)
        }
    }

    private fun onZeroBalanceSettingsChanged(bundle: Bundle) {
        val isZeroBalanceVisible = bundle.getBoolean(BUNDLE_KEY_IS_ZERO_BALANCE_VISIBLE)
        presenter.onZeroBalanceVisibilityChanged(isVisible = isZeroBalanceVisible)
    }
}
