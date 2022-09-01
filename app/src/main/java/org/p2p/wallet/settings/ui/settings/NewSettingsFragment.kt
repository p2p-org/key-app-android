package org.p2p.wallet.settings.ui.settings

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.ReserveMode
import org.p2p.wallet.auth.ui.username.ReserveUsernameFragment
import org.p2p.wallet.auth.ui.username.UsernameFragment
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSettingsBinding
import org.p2p.wallet.settings.ui.network.SettingsNetworkFragment
import org.p2p.wallet.settings.ui.settings.adapter.NewSettingsAdapter
import org.p2p.wallet.settings.ui.settings.adapter.SettingsItemClickListener
import org.p2p.wallet.settings.ui.settings.presenter.SettingsItem
import org.p2p.wallet.utils.BiometricPromptWrapper
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.requireSerializable
import org.p2p.wallet.utils.showInfoDialog
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding

class NewSettingsFragment :
    BaseMvpFragment<NewSettingsContract.View, NewSettingsContract.Presenter>(R.layout.fragment_settings),
    NewSettingsContract.View,
    SettingsItemClickListener {

    companion object {
        private const val REQUEST_KEY = "EXTRA_REQUEST_KEY"

        private const val KEY_NEW_NETWORK = "KEY_NEW_NETWORK"
        private const val KEY_PIN_CHANGED = "KEY_PIN_CHANGED"

        fun create(): NewSettingsFragment = NewSettingsFragment()
    }

    override val presenter: NewSettingsContract.Presenter by inject()

    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    private val binding: FragmentSettingsBinding by viewBinding()

    private val adapter = NewSettingsAdapter(this)

    private val biometricWrapper: BiometricPromptWrapper by unsafeLazy {
        BiometricPromptWrapper(
            fragment = this,
            onSuccess = { presenter.onBiometricSignInEnableConfirmed(EncodeCipher(it)) },
            onError = { message ->
                message?.let { showUiKitSnackBar(message = it) }
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().supportFragmentManager.setFragmentResultListener(
            REQUEST_KEY,
            viewLifecycleOwner,
            ::handleFragmentResult
        )
    }

    private fun handleFragmentResult(requestKey: String, result: Bundle) {
        when {
            result.containsKey(KEY_NEW_NETWORK) -> {
                presenter.onNetworkEnvironmentChanged(result.requireSerializable(KEY_NEW_NETWORK))
            }
            result.containsKey(KEY_PIN_CHANGED) -> {
                // TODO: PWN-4716
            }
        }
    }

    override fun showSettings(settings: List<SettingsItem>) {
        binding.recyclerViewSettings.attachAdapter(adapter)
        adapter.setItems(settings)
    }

    override fun onSettingsItemClicked(clickedSettings: SettingsItem) {
        when (clickedSettings) {
            is SettingsItem.ComplexSettingsItem -> handleNavigationForComplexSetting(clickedSettings)
            is SettingsItem.SignOutButtonItem -> presenter.onSignOutClicked()
            is SettingsItem.SwitchSettingsItem -> handleSwitchSetting(clickedSettings)
            else -> Unit
        }
    }

    private fun handleNavigationForComplexSetting(settings: SettingsItem.ComplexSettingsItem) {
        when (settings.settingNameRes) {
            R.string.settings_item_title_username -> {
                presenter.onUsernameSettingClicked()
            }
            R.string.settings_item_title_pin -> {
                // todo: PWN-4716
            }
            R.string.settings_item_title_networks -> {
                analyticsInteractor.logScreenOpenEvent(ScreenNames.Settings.NETWORK)
                addFragment(
                    SettingsNetworkFragment.create(requestKey = REQUEST_KEY, resultKey = KEY_NEW_NETWORK),
                    enter = 0, exit = 0, popEnter = 0, popExit = 0
                )
            }
        }
    }

    private fun handleSwitchSetting(settings: SettingsItem.SwitchSettingsItem) {
        when (settings.settingNameRes) {
            R.string.settings_item_title_touch_id -> {
                presenter.onBiometricSignInSwitchChanged(settings.isSwitched)
            }
            R.string.settings_item_title_zero_balances -> {
                presenter.changeZeroBalanceHiddenFlag(settings.isSwitched)
            }
        }
    }

    override fun confirmBiometrics(pinCodeCipher: EncodeCipher) {
        biometricWrapper.authenticate(pinCodeCipher.value)
    }

    override fun showSignOutConfirmDialog() {
        showInfoDialog(
            titleRes = R.string.settings_logout_title,
            messageRes = R.string.settings_logout_message,
            primaryButtonRes = R.string.common_logout,
            primaryCallback = { presenter.onConfirmSignOutClicked() },
            secondaryButtonRes = R.string.common_stay,
            primaryButtonTextColor = R.color.systemErrorMain
        )
    }

    override fun openReserveUsernameScreen() {
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Settings.USERNAME_RESERVE)
        replaceFragment(ReserveUsernameFragment.create(mode = ReserveMode.POP, isSkipStepEnabled = false))
    }

    override fun openUsernameScreen() {
        replaceFragment(UsernameFragment.create())
    }
}
