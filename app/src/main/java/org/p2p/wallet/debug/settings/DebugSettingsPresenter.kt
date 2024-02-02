package org.p2p.wallet.debug.settings

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.core.network.environment.NetworkEnvironment
import org.p2p.core.network.environment.NetworkEnvironmentManager
import org.p2p.core.network.environment.NetworkServicesUrlProvider
import org.p2p.wallet.R
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.user.repository.UserTokensLocalRepository

class DebugSettingsPresenter(
    private val interactor: DebugSettingsInteractor,
    private val environmentManager: NetworkEnvironmentManager,
    private val deviceInfoMapper: DebugSettingsDeviceInfoMapper,
    private val userTokensLocalRepository: UserTokensLocalRepository,
    private val networkServicesUrlProvider: NetworkServicesUrlProvider,
    private val inAppFeatureFlags: InAppFeatureFlags,
    private val appRestarter: AppRestarter,
    private val settingsMapper: DebugSettingsMapper,
) : BasePresenter<DebugSettingsContract.View>(), DebugSettingsContract.Presenter {

    private var networkName = environmentManager.loadCurrentEnvironment().name

    override fun loadData() {
        val settings = settingsMapper.mapMainSettings() +
            deviceInfoMapper.mapAppInfo() +
            deviceInfoMapper.mapDeviceInfo() +
            deviceInfoMapper.mapCiInfo()
        view?.showSettings(settings)
    }

    override fun onNetworkChanged(newNetworkEnvironment: NetworkEnvironment) {
        this.networkName = newNetworkEnvironment.name

        launch {
            try {
                environmentManager.chooseEnvironment(newNetworkEnvironment)
                userTokensLocalRepository.clear()
            } catch (error: Throwable) {
                Timber.e(error, "Network changing failed")
            }
        }

        loadData()
    }

    override fun onSettingsSwitchClicked(titleResId: Int, isChecked: Boolean) {
        when (titleResId) {
            R.string.debug_settings_fee_relayer -> {
                networkServicesUrlProvider.toggleFeeRelayerEnvironment(isChecked)
            }
            R.string.debug_settings_notification_service -> {
                networkServicesUrlProvider.toggleNotificationServiceEnvironment(isChecked)
            }
            R.string.debug_settings_moonpay_sandbox -> {
                networkServicesUrlProvider.toggleMoonpayEnvironment(isChecked)
            }
            R.string.debug_settings_name_service -> {
                networkServicesUrlProvider.toggleNameServiceEnvironment(isChecked)
            }
            R.string.debug_settings_token_service -> {
                networkServicesUrlProvider.toggleTokenServiceEnvironment(isChecked)
            }
        }
        appRestarter.restartApp()
    }

    override fun onSettingsPopupMenuClicked(selectedValue: String) {
        if (selectedValue != "-") {
            inAppFeatureFlags.strigaKycBannerMockFlag.featureValueString = selectedValue
        } else {
            inAppFeatureFlags.strigaKycBannerMockFlag.featureValueString = null
        }
    }

    override fun onClickSetKycRejected() {
        launch {
            try {
                interactor.setStrigaKycRejected()
                view?.showUiKitSnackBar("Status successfully changed")
            } catch (e: Throwable) {
                Timber.d(e)
                view?.showErrorMessage(e)
            }
        }
    }

    override fun onClickDetachStrigaUser() {
        launch {
            try {
                interactor.detachStrigaUserFromMetadata()
                view?.showUiKitSnackBar("Striga user is successfully detached")
                appRestarter.restartApp()
            } catch (e: Throwable) {
                view?.showUiKitSnackBar("Metadata is not loaded. Unable to proceed.")
            }
        }
    }

    override fun onClickResetUserCountry() {
        interactor.resetUserCountry()
        view?.showUiKitSnackBar(message = "User country has been reset")
    }

    override fun onSwapUrlChanged(urlValue: String) {
        if (urlValue.isNotBlank()) {
            networkServicesUrlProvider.saveSwapEnvironment(urlValue)
            loadData()
        }
    }
}
