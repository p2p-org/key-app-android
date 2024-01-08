package org.p2p.wallet.utils

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.p2p.core.BuildConfig
import org.p2p.core.common.di.AppScope
import org.p2p.core.crashlytics.CrashLogger
import org.p2p.core.network.environment.NetworkServicesUrlProvider
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage

/**
 * Init keys and tags for crash logger
 */
class CrashLoggerInitializer(
    private val crashLogger: CrashLogger,
    private val usernameInteractor: UsernameInteractor,
    private val networkServicesUrlProvider: NetworkServicesUrlProvider,
    private val web3SignupData: UserSignUpDetailsStorage,
    private val appScope: AppScope
) {

    fun init() {
        crashLogger.runCatching {
            setCustomKey("crashlytics_enabled", BuildConfig.CRASHLYTICS_ENABLED)

            val torusEnv = networkServicesUrlProvider.loadTorusEnvironment()
            setCustomKey("verifier", torusEnv.verifier)
            setCustomKey("sub_verifier", torusEnv.subVerifier.orEmpty())
            setCustomKey("username", usernameInteractor.getUsername()?.fullUsername.orEmpty())

            web3SignupData.observeSignUpUserDetails()
                .map { it?.signUpDetails }
                .onEach {
                    setCustomKey("web3_eth_pub_key", it?.ethereumPublicKey ?: "-")
                    setCustomKey("is_web_3_auth", (it != null).toString())
                }
                .launchIn(appScope)
        }
    }
}
