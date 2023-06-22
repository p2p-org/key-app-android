package org.p2p.wallet.settings.ui.security

import android.content.res.Resources
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.interactor.AuthLogoutInteractor
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseSource
import org.p2p.wallet.settings.DeviceInfoHelper

class SecurityAndPrivacyPresenter(
    private val resources: Resources,
    private val seedPhraseProvider: SeedPhraseProvider,
    private val adminAnalytics: AdminAnalytics,
    private val appRestarter: AppRestarter,
    private val authLogoutInteractor: AuthLogoutInteractor,
    private val metadataInteractor: MetadataInteractor,
) : BasePresenter<SecurityAndPrivacyContract.View>(),
    SecurityAndPrivacyContract.Presenter {

    private var seedPhraseProviderType: SeedPhraseSource? = null
    private val seedPhrase = mutableListOf<SeedPhraseWord>()

    override fun onSeedPhraseClicked() {
        if (seedPhraseProvider.isAvailable) {
            view?.showSeedPhraseLockFragment()
        } else {
            view?.showLogoutInfoDialog()
        }
    }

    override fun attach(view: SecurityAndPrivacyContract.View) {
        super.attach(view)
        loadSeedPhrase()
        if (seedPhraseProvider.isWeb3AuthUser) {
            loadMetadata()
        }
    }

    private fun loadSeedPhrase() {
        val userSeedPhrase = seedPhraseProvider.getUserSeedPhrase()
        seedPhraseProviderType = userSeedPhrase.provider
        seedPhrase.clear()
        seedPhrase += userSeedPhrase.seedPhrase.map { SeedPhraseWord(it, isValid = true) }
    }

    private fun setUnavailableState() {
        val notAvailableString = resources.getString(R.string.recovery_not_available)
        view?.apply {
            showDeviceName(notAvailableString, showWarning = false)
            showPhoneNumber(notAvailableString)
            showSocialId(notAvailableString)
        }
    }

    private fun loadMetadata() {
        view?.setWebAuthInfoVisibility(isVisible = true)

        val metadata = metadataInteractor.currentMetadata ?: return setUnavailableState()

        val isChangeEnabled = metadataInteractor.hasDifferentDeviceShare()
        view?.showDeviceName(metadata.deviceShareDeviceName, showWarning = isChangeEnabled)
        view?.showPhoneNumber(metadata.customSharePhoneNumberE164)
        view?.showSocialId(metadata.socialShareOwnerEmail)

        view?.showManageVisible(isVisible = isChangeEnabled)
    }

    override fun logout() {
        authLogoutInteractor.onUserLogout()
        adminAnalytics.logSignedOut()
        appRestarter.restartApp()
    }
}
