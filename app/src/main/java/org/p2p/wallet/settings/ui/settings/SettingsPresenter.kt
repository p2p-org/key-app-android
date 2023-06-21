package org.p2p.wallet.settings.ui.settings

import timber.log.Timber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.interactor.AuthLogoutInteractor
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.core.network.environment.NetworkEnvironment
import org.p2p.core.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.settings.model.SettingsItemMapper

private const val NETWORK_CHANGE_DELAY = 250L

class SettingsPresenter(
    private val environmentManager: NetworkEnvironmentManager,
    private val usernameInteractor: UsernameInteractor,
    private val authLogoutInteractor: AuthLogoutInteractor,
    private val appRestarter: AppRestarter,
    private val analytics: SettingsPresenterAnalytics,
    private val settingsInteractor: SettingsInteractor,
    private val homeLocalRepository: HomeLocalRepository,
    private val settingsItemMapper: SettingsItemMapper,
    private val metadataInteractor: MetadataInteractor,
    private val authInteractor: AuthInteractor
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
                isBiometricLoginEnabled = settingsInteractor.isBiometricLoginEnabled(),
                isBiometricLoginAvailable = settingsInteractor.isBiometricLoginAvailable(),
                isZeroBalanceTokenHidden = settingsInteractor.areZerosHidden(),
                hasDifferentDeviceShare = metadataInteractor.hasDifferentDeviceShare()
            )
            view?.showSettings(settings)
        } catch (error: Throwable) {
            Timber.e(error, "Error loading settings screen")
            view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
        }
    }

    override fun changeZeroBalanceHiddenFlag(hideValue: Boolean) {
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
    }

    override fun onSecurityClicked() {
        view?.openSecurityAndPrivacy()
    }

    override fun onNetworkEnvironmentChanged(newNetworkEnvironment: NetworkEnvironment) {
        launch {
            try {
                environmentManager.chooseEnvironment(newNetworkEnvironment)

                homeLocalRepository.clear()

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
