package org.p2p.wallet.settings.ui.settings

import android.content.Context
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.interactor.AuthLogoutInteractor
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironment
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.renbtc.service.RenVMService
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.settings.model.SettingsItemMapper
import timber.log.Timber

private const val NETWORK_CHANGE_DELAY = 250L

class NewSettingsPresenter(
    private val environmentManager: NetworkEnvironmentManager,
    private val usernameInteractor: UsernameInteractor,
    private val authLogoutInteractor: AuthLogoutInteractor,
    private val appRestarter: AppRestarter,
    private val receiveAnalytics: ReceiveAnalytics,
    private val adminAnalytics: AdminAnalytics,
    private val browseAnalytics: BrowseAnalytics,
    private val settingsInteractor: SettingsInteractor,
    private val homeLocalRepository: HomeLocalRepository,
    private val settingsItemMapper: SettingsItemMapper,
    private val authInteractor: AuthInteractor,
    private val seedPhraseProvider: SeedPhraseProvider,
    private val context: Context
) : BasePresenter<NewSettingsContract.View>(), NewSettingsContract.Presenter {

    override fun attach(view: NewSettingsContract.View) {
        super.attach(view)
        launch { loadSettings() }
    }

    private suspend fun loadSettings() {
        try {
            val settings = settingsItemMapper.createItems(
                username = usernameInteractor.getUsername(),
                isUsernameItemVisible = usernameInteractor.isUsernameItemVisibleInSettings(),
                isBiometricLoginEnabled = settingsInteractor.isBiometricLoginEnabled(),
                isBiometricLoginAvailable = settingsInteractor.isBiometricLoginAvailable(),
                isZeroBalanceTokenHidden = settingsInteractor.areZerosHidden()
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
        launch {
            try {
                authInteractor.enableFingerprintSignIn(biometricsCipher)
                loadSettings()
            } catch (fingerPrintChangeError: Throwable) {
                Timber.e(fingerPrintChangeError, "Failed to change biometric login flag")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }
    }

    override fun onSignOutClicked() {
        adminAnalytics.logSignOut()
        view?.showSignOutConfirmDialog()
    }

    override fun onConfirmSignOutClicked() {
        authLogoutInteractor.onUserLogout()
        adminAnalytics.logSignedOut()
        appRestarter.restartApp()
    }

    override fun onUsernameSettingClicked() {
        val isUsernameExists = usernameInteractor.isUsernameExist()
        if (isUsernameExists) view?.openUsernameScreen() else view?.openReserveUsernameScreen()
        receiveAnalytics.logSettingsUsernameViewed(isUsernameExists)
    }

    override fun onRecoveryKitClicked() {
        view?.openRecoveryKitScreen()
    }

    override fun onNetworkEnvironmentChanged(newNetworkEnvironment: NetworkEnvironment) {
        launch {
            try {
                environmentManager.chooseEnvironment(newNetworkEnvironment)

                homeLocalRepository.clear()
                RenVMService.stopService(context)

                browseAnalytics.logNetworkChanging(newNetworkEnvironment.name)
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
