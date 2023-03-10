package org.p2p.wallet.auth.ui.onboarding.root

import android.net.Uri
import kotlinx.coroutines.launch
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.wallet.auth.analytics.OnboardingAnalytics
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import org.p2p.wallet.deeplinks.DeeplinkQuery
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseSource
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.restore.interactor.SeedPhraseInteractor
import org.p2p.wallet.restore.model.SeedPhraseVerifyResult
import org.p2p.wallet.utils.emptyString

class OnboardingRootPresenter(
    private val userSignUpDetailsStorage: UserSignUpDetailsStorage,
    private val tokenKeyProvider: TokenKeyProvider,
    private val authInteractor: AuthInteractor,
    private val onboardingAnalytics: OnboardingAnalytics,
    private val deeplinksManager: AppDeeplinksManager,
    private val seedPhraseInteractor: SeedPhraseInteractor,
    private val seedPhraseProvider: SeedPhraseProvider
) : BasePresenter<OnboardingRootContract.View>(), OnboardingRootContract.Presenter {

    override fun attach(view: OnboardingRootContract.View) {
        super.attach(view)
        // pass empty string as UserId to launch IntercomService as anonymous user
        IntercomService.signIn(emptyString())

        val userDetails = userSignUpDetailsStorage.getLastSignUpUserDetails()
        onboardingAnalytics.setUserHasDeviceShare(
            hasDeviceShare = userDetails?.signUpDetails?.deviceShare != null
        )
        when {
            userLeftOnPinCreation() -> view.navigateToCreatePin()
            userDetails != null -> handleUserDetailsExist(userDetails)
            else -> view.navigateToOnboarding()
        }

        // Sign in unidentified user for help messenger in onboarding flow
        IntercomService.signIn(emptyString())
    }

    private fun handleUserDetailsExist(userDetails: UserSignUpDetailsStorage.SignUpUserDetails) {
        when {
            userSignUpDetailsStorage.isSignUpInProcess() -> view?.navigateToContinueOnboarding()
            userDetails.signUpDetails.deviceShare != null -> view?.navigateToRestore()
            else -> view?.navigateToOnboarding()
        }
    }

    private fun userLeftOnPinCreation(): Boolean {
        return tokenKeyProvider.keyPair.isNotEmpty() && !authInteractor.isAuthorized()
    }

    override fun validDeeplink(deeplink: Uri) {
        val seedPhrase = deeplink.getQueryParameter(DeeplinkQuery.value) ?: return
        if (seedPhrase.isBlank()) return

        launch {
            val keys = seedPhrase.split("-").map { key ->
                SeedPhraseWord(
                    text = key,
                    isValid = true
                )
            }
            verifySeedPhrase(keys, deeplink)
        }
    }

    private suspend fun verifySeedPhrase(seedPhrase: List<SeedPhraseWord>, deeplink: Uri) {
        when (val result = seedPhraseInteractor.verifySeedPhrase(seedPhrase)) {
            is SeedPhraseVerifyResult.Verified -> {
                val keys = result.getKeys()
                seedPhraseProvider.setUserSeedPhrase(
                    words = keys,
                    provider = SeedPhraseSource.MANUAL
                )
                seedPhraseInteractor.createAndSaveAccount(DerivationPath.BIP44CHANGE, keys)
                createPin(deeplink)
            }
            is SeedPhraseVerifyResult.Invalid,
            is SeedPhraseVerifyResult.VerificationFailed -> Unit
        }
    }

    private fun createPin(deeplink: Uri) {
        val pinCode = deeplink.getQueryParameter(DeeplinkQuery.pinCode) ?: return
        if (pinCode.isNotEmpty()) {
            authInteractor.registerComplete(pinCode, cipher = null)
            authInteractor.finishSignUp()
            view?.navigateToMain()
        }
    }
}
