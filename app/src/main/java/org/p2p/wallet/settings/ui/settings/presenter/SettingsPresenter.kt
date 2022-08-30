package org.p2p.wallet.settings.ui.settings.presenter

import android.content.Context
import org.p2p.solanaj.rpc.NetworkEnvironment
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.interactor.AuthLogoutInteractor
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.renbtc.service.RenVMService
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.settings.ui.settings.SettingsContract
import timber.log.Timber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val NETWORK_CHANGE_DELAY = 250L

class SettingsPresenter(
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
    private val context: Context
) : BasePresenter<SettingsContract.View>(), SettingsContract.Presenter {

    override fun attach(view: SettingsContract.View) {
        super.attach(view)
        loadSettings()
    }

    private fun loadSettings() {
        launch {
            val settings = settingsItemMapper.createItems(
                username = usernameInteractor.getUsername(),
                isBiometricConfirmEnabled = settingsInteractor.isBiometricsConfirmationEnabled(),
                isZeroBalanceTokenHidden = settingsInteractor.areZerosHidden()
            )
            view?.showSettings(settings)
        }
    }

    override fun changeZeroBalanceHiddenFlag(hideValue: Boolean) {
        settingsInteractor.setZeroBalanceHidden(hideValue)
        loadSettings()
    }

    override fun changeBiometricConfirmFlag(isBiometricConfirmNeeded: Boolean) {
        settingsInteractor.setBiometricsConfirmationEnabled(isBiometricConfirmNeeded)
        loadSettings()
    }

    override fun onSignOutClicked() {
        adminAnalytics.logSignOut()
        view?.showSignOutConfirmDialog()
    }

    override fun onConfirmSignOutClicked() {
        try {
            authLogoutInteractor.onUserLogout()
            adminAnalytics.logSignedOut()
        } catch (error: Throwable) {
            Timber.e(error, "Error signing out")
        } finally {
            appRestarter.restartApp()
        }
    }

    override fun onUsernameSettingClicked() {
        val isUsernameExists = usernameInteractor.isUsernameExists()
        if (isUsernameExists) view?.openUsernameScreen() else view?.openReserveUsernameScreen()
        receiveAnalytics.logSettingsUsernameViewed(isUsernameExists)
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
