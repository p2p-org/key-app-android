package org.p2p.wallet.splash

import android.net.Uri
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import org.p2p.wallet.deeplinks.DeeplinkQuery
import org.p2p.wallet.deeplinks.DeeplinkUtils
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseSource
import org.p2p.wallet.restore.interactor.SeedPhraseInteractor
import org.p2p.wallet.user.interactor.UserInteractor
import timber.log.Timber

private const val MINIMUM_SPLASH_SHOWING_TIME_MS = 2000L

class SplashPresenter(
    private val authInteractor: AuthInteractor,
    private val userInteractor: UserInteractor,
    private val deeplinksManager: AppDeeplinksManager,
    private val seedPhraseInteractor: SeedPhraseInteractor,
    private val seedPhraseProvider: SeedPhraseProvider
) : BasePresenter<SplashContract.View>(), SplashContract.Presenter {

    override fun attach(view: SplashContract.View) {
        super.attach(view)
        launch {
            delay(MINIMUM_SPLASH_SHOWING_TIME_MS)
            loadPricesAndBids()
        }
    }

    private fun loadPricesAndBids() {
        launch {
            try {
                userInteractor.loadAllTokensData()
            } catch (e: Throwable) {
                Timber.e(e, "Error loading initial tokens data")
            } finally {
                if (DeeplinkUtils.hasFastOnboardingDeeplink(deeplinksManager.pendingDeeplinkUri)) {
                    handleDeeplink()
                } else {
                    openRootScreen()
                }
            }
        }
    }

    private fun openRootScreen() {
        if (authInteractor.isAuthorized()) {
            view?.navigateToSignIn()
        } else {
            view?.navigateToOnboarding()
        }
    }

    private suspend fun handleDeeplink() {
        deeplinksManager.pendingDeeplinkUri?.let { deeplinkUri ->
            deeplinkUri.getQueryParameter(DeeplinkQuery.value)?.let { seed ->
                if (seed.isNotEmpty()) {
                    val keys = seed.split("-").map {
                        SeedPhraseWord(
                            text = it,
                            isValid = true,
                            isBlurred = false
                        )
                    }
                    verifySeedPhrase(keys, deeplinkUri)
                    return
                }
            }
        }
        openRootScreen()
    }

    private suspend fun verifySeedPhrase(seedPhrase: List<SeedPhraseWord>, deeplink: Uri) {
        var currentSeedPhrase = seedPhrase
        when (val result = seedPhraseInteractor.verifySeedPhrase(currentSeedPhrase)) {
            is SeedPhraseInteractor.SeedPhraseVerifyResult.VerifiedSeedPhrase -> {
                currentSeedPhrase = result.seedPhraseWord
                if (currentSeedPhrase.all(SeedPhraseWord::isValid)) {
                    val secretKeys = currentSeedPhrase.map { it.text }
                    seedPhraseProvider.setUserSeedPhrase(
                        words = secretKeys,
                        provider = SeedPhraseSource.MANUAL
                    )
                    seedPhraseInteractor.createAndSaveAccount(DerivationPath.BIP44CHANGE, secretKeys)
                    createPin(deeplink)
                } else {
                    openRootScreen()
                }
            }
            else -> openRootScreen()
        }
    }

    private fun createPin(deeplink: Uri) {
        deeplink.getQueryParameter(DeeplinkQuery.pinCode)?.let { pinCode ->
            if (pinCode.isNotEmpty()) {
                authInteractor.registerComplete(pinCode, cipher = null)
                authInteractor.finishSignUp()
                deeplinksManager.pendingDeeplinkUri = null
                view?.navigateToMain()
            }
        } ?: openRootScreen()
    }
}
