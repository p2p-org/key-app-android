package org.p2p.wallet

import org.koin.core.component.KoinComponent
import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.core.crashlytics.CrashLogger
import org.p2p.core.common.di.AppScope
import org.p2p.wallet.common.feature_toggles.remote_config.AppFirebaseRemoteConfig
import org.p2p.wallet.common.feature_toggles.toggles.remote.SolendEnabledFeatureToggle
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.intercom.IntercomPushService
import org.p2p.wallet.push_notifications.repository.PushTokenRepository
import org.p2p.wallet.solend.repository.SolendConfigurationRepository
import org.p2p.core.crypto.toBase58Instance

/**
 * Entity to perform some actions / loading needed when app is created
 */
class AppCreatedAction(
    private val pushTokenRepository: PushTokenRepository,
    private val remoteConfig: AppFirebaseRemoteConfig,
    private val solendConfigRepository: SolendConfigurationRepository,
    private val solendFeatureToggle: SolendEnabledFeatureToggle,
    private val usernameInteractor: UsernameInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val crashLogger: CrashLogger,
    private val intercomPushService: IntercomPushService,
    private val appScope: AppScope,
) : KoinComponent {

    operator fun invoke() {
        if (BuildConfig.DEBUG) {
            logFirebaseDevicePushToken()
        }

        remoteConfig.loadRemoteConfig(onConfigLoaded = { toggles ->
            if (solendFeatureToggle.isFeatureEnabled) {
                initSolend()
            }
            toggles.forEach { (toggleKey, toggleValue) -> crashLogger.setCustomKey(toggleKey, toggleValue) }
        })

        tryRestoreUsername()
    }

    private fun initSolend() {
        appScope.launch {
            try {
                solendConfigRepository.loadSolendConfiguration()
            } catch (error: Throwable) {
                Timber.e(error, "Failed to init Solend when app is created")
            }
        }
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
}
