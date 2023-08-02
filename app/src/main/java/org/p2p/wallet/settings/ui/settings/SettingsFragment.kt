package org.p2p.wallet.settings.ui.settings

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.analytics.constants.ScreenNames
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.ui.reserveusername.ReserveUsernameFragment
import org.p2p.wallet.auth.ui.reserveusername.ReserveUsernameOpenedFrom
import org.p2p.wallet.auth.ui.username.UsernameFragment
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSettingsBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.settings.model.SettingsItem
import org.p2p.wallet.settings.ui.network.SettingsNetworkBottomSheet
import org.p2p.wallet.settings.ui.resetpin.main.ResetPinIntroFragment
import org.p2p.wallet.settings.ui.security.SecurityAndPrivacyFragment
import org.p2p.wallet.settings.ui.settings.adapter.NewSettingsAdapter
import org.p2p.wallet.striga.signup.presetpicker.StrigaPresetDataPickerFragment
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaPresetDataItem
import org.p2p.wallet.utils.BiometricPromptWrapper
import org.p2p.wallet.utils.getParcelableCompat
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.replaceFragmentForResult
import org.p2p.wallet.utils.requireParcelable
import org.p2p.wallet.utils.showInfoDialog
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding

private const val REQUEST_KEY = "EXTRA_REQUEST_KEY"
private const val RESULT_KEY_NEW_NETWORK = "KEY_NEW_NETWORK"
private const val REQUEST_COUNTRY_PICKER = "COUNTRY_PICKER_REQUEST_KEY"
private const val RESULT_COUNTRY_PICKER = "COUNTRY_PICKER_RESULT_KEY"

class SettingsFragment :
    BaseMvpFragment<SettingsContract.View, SettingsContract.Presenter>(R.layout.fragment_settings),
    SettingsContract.View {

    companion object {
        fun create(): SettingsFragment = SettingsFragment()
    }

    override val presenter: SettingsContract.Presenter by inject()

    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    private val binding: FragmentSettingsBinding by viewBinding()

    private val adapter = NewSettingsAdapter(::onSettingsItemClicked)

    private val biometricWrapper: BiometricPromptWrapper by unsafeLazy {
        BiometricPromptWrapper(
            fragment = this,
            onSuccess = { presenter.onBiometricSignInEnableConfirmed(EncodeCipher(it)) },
            onError = { message ->
                message?.let { showUiKitSnackBar(message = it) }
                presenter.onBiometricSignInSwitchChanged(isSwitched = false)
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.setFragmentResultListener(REQUEST_KEY, viewLifecycleOwner) { _, bundle ->
            if (bundle.containsKey(RESULT_KEY_NEW_NETWORK)) {
                presenter.onNetworkEnvironmentChanged(bundle.requireParcelable(RESULT_KEY_NEW_NETWORK))
            }
        }
    }

    override fun showSettings(settings: List<SettingsItem>) {
        binding.recyclerViewSettings.attachAdapter(adapter)
        adapter.setItems(settings)
    }

    override fun showCountryPicker(countryCode: CountryCode?) {
        replaceFragmentForResult(
            target = StrigaPresetDataPickerFragment.create(
                requestKey = REQUEST_COUNTRY_PICKER,
                resultKey = RESULT_COUNTRY_PICKER,
                dataToPick = StrigaPresetDataItem.Country(countryCode)
            ),
            requestKey = REQUEST_COUNTRY_PICKER,
            onResult = { _, result ->
                result.getParcelableCompat<StrigaPresetDataItem.Country>(RESULT_COUNTRY_PICKER)
                    ?.details
                    ?.also { presenter.onCountryChanged(it) }
            }
        )
    }

    private fun onSettingsItemClicked(clickedSettings: SettingsItem) {
        when (clickedSettings) {
            is SettingsItem.ComplexSettingsItem -> handleNavigationForComplexItem(clickedSettings)
            is SettingsItem.SignOutButtonItem -> presenter.onSignOutClicked()
            is SettingsItem.SwitchSettingsItem -> handleSwitchItem(clickedSettings)
            else -> Unit
        }
    }

    private fun handleNavigationForComplexItem(settings: SettingsItem.ComplexSettingsItem) {
        when (settings.nameRes) {
            R.string.settings_item_title_security -> {
                presenter.onSecurityClicked()
            }
            R.string.settings_item_title_username -> {
                presenter.onUsernameSettingClicked()
            }
            R.string.settings_item_title_country -> {
                presenter.onCountryClicked()
            }
            R.string.settings_item_title_pin -> {
                presenter.onPinClicked()
            }
            R.string.settings_item_title_networks -> {
                presenter.onNetworkClicked()
            }
            R.string.settings_item_title_support -> {
                presenter.onSupportClicked()
            }
            R.string.settings_item_title_twitter -> {
                presenter.onOpenTwitterClicked()
            }
            R.string.settings_item_title_discord -> {
                presenter.onOpenDiscordClicked()
            }
        }
    }

    private fun handleSwitchItem(settings: SettingsItem.SwitchSettingsItem) {
        when (settings.nameRes) {
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

    override fun updateSwitchItem(switchItemId: Int, isSwitched: Boolean) {
        adapter.updateSwitchItem(switchItemId, isSwitched)
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
        replaceFragment(ReserveUsernameFragment.create(ReserveUsernameOpenedFrom.SETTINGS))
    }

    override fun openPinScreen() {
        replaceFragment(ResetPinIntroFragment.create())
    }

    override fun openNetworkScreen() {
        SettingsNetworkBottomSheet.show(
            fm = childFragmentManager,
            requestKey = REQUEST_KEY,
            resultKey = RESULT_KEY_NEW_NETWORK
        )
    }

    override fun openSupportScreen() {
        IntercomService.showMessenger()
    }

    override fun openTwitterScreen() {
        requireContext().showUrlInCustomTabs("https://twitter.com/KeyApp_")
    }

    override fun openDiscordScreen() {
        val discordLink = "https://discord.gg/SpW3GmEYgU"
        requireContext().showUrlInCustomTabs(discordLink)
    }

    override fun openUsernameScreen() {
        replaceFragment(UsernameFragment.create())
    }

    override fun openSecurityAndPrivacy() {
        replaceFragment(SecurityAndPrivacyFragment.create())
    }
}
