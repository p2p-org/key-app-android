package org.p2p.wallet

import org.koin.core.component.KoinComponent
import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.core.crashlytics.CrashLogger
import org.p2p.core.crypto.toBase58Instance
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.feature_toggles.remote_config.AppFirebaseRemoteConfig
import org.p2p.wallet.common.storage.ExternalStorageRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorageContract
import org.p2p.wallet.infrastructure.swap.KEY_ROUTES_FETCH_DATE
import org.p2p.wallet.infrastructure.swap.KEY_ROUTES_MINTS
import org.p2p.wallet.infrastructure.swap.KEY_SWAP_TOKENS
import org.p2p.wallet.intercom.IntercomPushService
import org.p2p.wallet.push_notifications.repository.PushTokenRepository

/**
 * Entity to perform some actions / loading needed when app is created
 */
class AppCreatedAction(
    private val pushTokenRepository: PushTokenRepository,
    private val remoteConfig: AppFirebaseRemoteConfig,
    private val usernameInteractor: UsernameInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val crashLogger: CrashLogger,
    private val intercomPushService: IntercomPushService,
    private val swapStorage: JupiterSwapStorageContract,
    private val fileRepository: ExternalStorageRepository,
    private val appScope: AppScope,
) : KoinComponent {

    operator fun invoke() {
        if (BuildConfig.DEBUG) {
            logFirebaseDevicePushToken()
        }

        remoteConfig.loadRemoteConfig(onConfigLoaded = { toggles ->
            toggles.forEach { (toggleKey, toggleValue) -> crashLogger.setCustomKey(toggleKey, toggleValue) }
        })

        removeDeprecatedData()

        tryRestoreUsername()
    }

    private fun logFirebaseDevicePushToken() {
        appScope.launch {
            try {
                val pushToken = pushTokenRepository.getPushToken()
                if (pushToken != null) {
                    intercomPushService.registerForPush(pushToken.value)
                    Timber.tag("App:device_token").d(pushToken.value)
                } else {
                    Timber.tag("App:device_token").d("Push token is null, skipping intercom registration")
                }
            } catch (error: Throwable) {
                Timber.e(error, "Failed to fetch push token")
            }
        }
    }

    private fun tryRestoreUsername() {
        appScope.launch {
            try {
                val userPublicKey = tokenKeyProvider.publicKey
                    .takeIf(String::isNotBlank)
                    ?.toBase58Instance()
                if (userPublicKey != null) {
                    usernameInteractor.tryRestoreUsername(userPublicKey)
                }
            } catch (error: Throwable) {
                Timber.e(error, "AppOnCreated tryRestoreUsername failed")
            }
        }
    }

    private fun removeDeprecatedData() {
        appScope.launch {
            fileRepository.deleteJsonFile("swap_routes.json")
            swapStorage.remove(KEY_SWAP_TOKENS)
            swapStorage.remove(KEY_ROUTES_MINTS)
            swapStorage.remove(KEY_ROUTES_FETCH_DATE)
        }
    }
}
