package org.p2p.wallet.settings.ui.settings

import org.p2p.core.network.environment.NetworkEnvironment
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.settings.model.SettingsItem

interface SettingsContract {
    interface View : MvpView {
        fun showSettings(settings: List<SettingsItem>)
        fun showCountryPicker(countryCode: CountryCode?)
        fun showSignOutConfirmDialog()
        fun openUsernameScreen()
        fun openSecurityAndPrivacy()
        fun openReserveUsernameScreen()
        fun openPinScreen()
        fun openNetworkScreen()
        fun openSupportScreen()
        fun openTwitterScreen()
        fun openDiscordScreen()
        fun confirmBiometrics(pinCodeCipher: EncodeCipher)
        fun updateSwitchItem(switchItemId: Int, isSwitched: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun onUsernameSettingClicked()
        fun onCountryClicked()
        fun onSecurityClicked()
        fun changeZeroBalanceHiddenFlag(hideValue: Boolean)
        fun onSignOutClicked()
        fun onConfirmSignOutClicked()
        fun onNetworkEnvironmentChanged(newNetworkEnvironment: NetworkEnvironment)
        fun onBiometricSignInSwitchChanged(isSwitched: Boolean)
        fun onBiometricSignInEnableConfirmed(biometricsCipher: EncodeCipher)
        fun onCountryChanged(selectedCountryCode: CountryCode)
        fun onPinClicked()
        fun onNetworkClicked()
        fun onSupportClicked()
        fun onOpenTwitterClicked()
        fun onOpenDiscordClicked()
    }
}
