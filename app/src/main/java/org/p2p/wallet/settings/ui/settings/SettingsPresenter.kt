package org.p2p.wallet.settings.ui.settings

import timber.log.Timber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.core.analytics.constants.ScreenNames
import org.p2p.core.network.environment.NetworkEnvironment
import org.p2p.core.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.interactor.AuthLogoutInteractor
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.common.feature_toggles.toggles.remote.ReferralProgramEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.settings.mapper.SettingsItemMapper
import org.p2p.wallet.settings.ui.settings.SettingsPresenterAnalytics.Companion.SETTING_ITEM_DISCORD
import org.p2p.wallet.settings.ui.settings.SettingsPresenterAnalytics.Companion.SETTING_ITEM_HIDE_BALANCE
import org.p2p.wallet.settings.ui.settings.SettingsPresenterAnalytics.Companion.SETTING_ITEM_NETWORK
import org.p2p.wallet.settings.ui.settings.SettingsPresenterAnalytics.Companion.SETTING_ITEM_PIN
import org.p2p.wallet.settings.ui.settings.SettingsPresenterAnalytics.Companion.SETTING_ITEM_SECURITY
import org.p2p.wallet.settings.ui.settings.SettingsPresenterAnalytics.Companion.SETTING_ITEM_SUPPORT
import org.p2p.wallet.settings.ui.settings.SettingsPresenterAnalytics.Companion.SETTING_ITEM_TWITTER
import org.p2p.wallet.settings.ui.settings.SettingsPresenterAnalytics.Companion.SETTING_ITEM_USERNAME
import org.p2p.wallet.user.repository.UserTokensLocalRepository

private const val NETWORK_CHANGE_DELAY = 250L

class SettingsPresenter(
    private val environmentManager: NetworkEnvironmentManager,
    private val usernameInteractor: UsernameInteractor,
    private val authLogoutInteractor: AuthLogoutInteractor,
    private val appRestarter: AppRestarter,
    private val analytics: SettingsPresenterAnalytics,
    private val settingsInteractor: SettingsInteractor,
    private val userTokensLocalRepository: UserTokensLocalRepository,
    private val settingsItemMapper: SettingsItemMapper,
    private val metadataInteractor: MetadataInteractor,
    private val authInteractor: AuthInteractor,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
    private val referralProgramEnabledFeatureToggle: ReferralProgramEnabledFeatureToggle,
) : BasePresenter<SettingsContract.View>(), SettingsContract.Presenter {

    override fun attach(view: SettingsContract.View) {
        super.attach(view)
        loadSettings()
    }

    private fun loadSettings() {
        try {
            val settings = settingsItemMapper.createItems(
                username = usernameInteractor.getUsername(),
                isUsernameItemVisible = usernameInteractor.isUsernameItemVisibleInSettings(),
                countryName = settingsInteractor.userCountryCode?.countryName,
                isBiometricLoginEnabled = settingsInteractor.isBiometricLoginEnabled(),
                isBiometricLoginAvailable = settingsInteractor.isBiometricLoginAvailable(),
                isZeroBalanceTokenHidden = settingsInteractor.areZerosHidden(),
                isReferralBannerVisible = referralProgramEnabledFeatureToggle.isFeatureEnabled,
                hasDifferentDeviceShare = metadataInteractor.hasDifferentDeviceShare()
            )
            view?.showSettings(settings)
        } catch (error: Throwable) {
            Timber.e(error, "Error loading settings screen")
            view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
        }
    }

    override fun changeZeroBalanceHiddenFlag(hideValue: Boolean) {
        analytics.logSettingItemClicked(SETTING_ITEM_HIDE_BALANCE)
        settingsInteractor.setZeroBalanceHidden(hideValue)
    }

    override fun onBiometricSignInSwitchChanged(isSwitched: Boolean) {
        if (isSwitched) {
            view?.confirmBiometrics(authInteractor.getPinEncodeCipher())
        } else {
            authInteractor.disableBiometricSignIn()
            view?.updateSwitchItem(R.string.settings_item_title_touch_id, isSwitched = false)
        }
    }

    override fun onBiometricSignInEnableConfirmed(biometricsCipher: EncodeCipher) {
        try {
            authInteractor.enableFingerprintSignIn(biometricsCipher)
            loadSettings()
        } catch (fingerPrintChangeError: Throwable) {
            Timber.e(fingerPrintChangeError, "Failed to change biometric login flag")
            view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
        }
    }

    override fun onCountryChanged(selectedCountryCode: CountryCode) {
        settingsInteractor.userCountryCode = selectedCountryCode
        loadSettings()
    }

    override fun onPinClicked() {
        analytics.logSettingItemClicked(SETTING_ITEM_PIN)
        view?.openPinScreen()
    }

    override fun onNetworkClicked() {
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Settings.NETWORK)
        view?.openNetworkScreen()
    }

    override fun onSupportClicked() {
        analytics.logSettingItemClicked(SETTING_ITEM_SUPPORT)
        view?.openSupportScreen()
    }

    override fun onOpenTwitterClicked() {
        analytics.logSettingItemClicked(SETTING_ITEM_TWITTER)
        view?.openTwitterScreen()
    }

    override fun onOpenDiscordClicked() {
        analytics.logSettingItemClicked(SETTING_ITEM_DISCORD)
        view?.openDiscordScreen()
    }

    override fun onSignOutClicked() {
        analytics.logSignOut()
        view?.showSignOutConfirmDialog()
    }

    override fun onConfirmSignOutClicked() {
        authLogoutInteractor.onUserLogout()
        analytics.logSignedOut()
        appRestarter.restartApp()
    }

    override fun onUsernameSettingClicked() {
        val isUsernameExists = usernameInteractor.isUsernameExist()
        if (isUsernameExists) view?.openUsernameScreen() else view?.openReserveUsernameScreen()
        analytics.logSettingsUsernameViewed(isUsernameExists)
        analytics.logSettingItemClicked(SETTING_ITEM_USERNAME)
    }

    override fun onCountryClicked() {
        view?.showCountryPicker(settingsInteractor.userCountryCode)
    }

    override fun onSecurityClicked() {
        view?.openSecurityAndPrivacy()
        analytics.logSettingItemClicked(SETTING_ITEM_SECURITY)
    }

    override fun onNetworkEnvironmentChanged(newNetworkEnvironment: NetworkEnvironment) {
        analytics.logSettingItemClicked(SETTING_ITEM_NETWORK)
        launch {
            try {
                environmentManager.chooseEnvironment(newNetworkEnvironment)

                userTokensLocalRepository.clear()

                analytics.logNetworkChanging(newNetworkEnvironment.name)
                // Sometimes these operations are completed too quickly
                // On the UI it shows blinking loading effect which is not good
                // Adding short delay to show loading state
                delay(NETWORK_CHANGE_DELAY)
            } catch (error: Throwable) {
                Timber.e(error, "Network changing failed")
            }
        }
    }
}
