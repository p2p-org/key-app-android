package org.p2p.wallet.settings.ui.recovery

import android.content.res.Resources
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.auth.interactor.AuthLogoutInteractor
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseSource
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import org.p2p.wallet.settings.DeviceInfoHelper

class RecoveryKitPresenter(
    private val secureStorage: SecureStorageContract,
    private val resources: Resources,
    private val seedPhraseProvider: SeedPhraseProvider,
    private val adminAnalytics: AdminAnalytics,
    private val appRestarter: AppRestarter,
    private val authLogoutInteractor: AuthLogoutInteractor,
    private val userSignUpDetailsStorage: UserSignUpDetailsStorage,
) : BasePresenter<RecoveryKitContract.View>(),
    RecoveryKitContract.Presenter {

    private var seedPhraseProviderType: SeedPhraseSource? = null
    private val seedPhrase = mutableListOf<SeedPhraseWord>()

    override fun onSeedPhraseClicked() {
        if (seedPhraseProvider.isAvailable) {
            view?.showSeedPhraseLockFragment()
        } else {
            view?.showLogoutInfoDialog()
        }
    }

    override fun attach(view: RecoveryKitContract.View) {
        super.attach(view)
        val userSeedPhrase = seedPhraseProvider.getUserSeedPhrase()
        seedPhraseProviderType = userSeedPhrase.provider
        seedPhrase.clear()
        seedPhrase.addAll(
            userSeedPhrase.seedPhrase.map {
                SeedPhraseWord(
                    text = it,
                    isValid = true,
                    isBlurred = false
                )
            }
        )
        fetchMetadata()
    }

    private fun setUnavailableState() {
        val notAvailableString = resources.getString(R.string.recovery_not_available)
        view?.apply {
            showDeviceName(notAvailableString, isDifferentFromDeviceShare = false)
            showPhoneNumber(notAvailableString)
            showSocialId(notAvailableString)
        }
    }

    private fun fetchMetadata() {
        secureStorage.getObject(
            SecureStorageContract.Key.KEY_ONBOARDING_METADATA,
            GatewayOnboardingMetadata::class
        )?.let { metadata ->
            val areDevicesSame = DeviceInfoHelper.getCurrentDeviceName() == metadata.deviceShareDeviceName
            view?.showDeviceName(metadata.deviceShareDeviceName, isDifferentFromDeviceShare = !areDevicesSame)
            view?.showPhoneNumber(metadata.customSharePhoneNumberE164)
            view?.showSocialId(metadata.socialShareOwnerEmail)
            val userDetails = userSignUpDetailsStorage.getLastSignUpUserDetails()
            val hasDeviceShare = userDetails?.signUpDetails?.deviceShare != null
            view?.showManageDevice(!hasDeviceShare && !areDevicesSame)
        } ?: setUnavailableState()

        view?.setWebAuthInfoVisibility(isVisible = true)
    }

    override fun logout() {
        authLogoutInteractor.onUserLogout()
        adminAnalytics.logSignedOut()
        appRestarter.restartApp()
    }
}
