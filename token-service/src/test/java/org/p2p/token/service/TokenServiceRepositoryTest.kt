package org.p2p.token.service

import android.content.Context
import android.content.SharedPreferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import timber.log.Timber
import kotlinx.coroutines.test.runTest
import org.p2p.core.R
import org.p2p.core.crashlytics.CrashLoggerModule
import org.p2p.core.network.NetworkCoreModule
import org.p2p.core.network.NetworkServicesUrlStorage
import org.p2p.core.network.environment.NetworkEnvironment
import org.p2p.core.network.environment.NetworkEnvironmentManager
import org.p2p.token.service.mock.NetworkEnvironmentStorageMock
import org.p2p.token.service.repository.TokenServiceRepository
import org.p2p.token.service.repository.TokenServiceRepositoryImpl

@RunWith(JUnit4::class)
class TokenServiceRepositoryTest : KoinTest {

    private val interactor: TokenServiceRepository by inject()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Timber.plant(Timber.DebugTree())
        val context = mockk<Context>() {
            coEvery { getString(R.string.tokenServiceBaseUrl) } returns "https://token-service.keyapp.org/"
        }
        val mockPrefs = mockk<SharedPreferences>() {
            coEvery {
                getString(
                    "KEY_TOKEN_SERVICE_BASE_URL",
                    "https://token-service.keyapp.org/"
                )
            } returns "https://token-service.keyapp.org/"
        }
        startKoin {
            androidContext(context)
            loadKoinModules(
                listOf(
                    NetworkCoreModule.create(),
                    CrashLoggerModule.create(),
                    TokenServiceModule.create(),
                    module {
                        single {
                            NetworkEnvironmentManager(
                                networkEnvironmentStorage = NetworkEnvironmentStorageMock(),
                                crashLogger = get(),
                                networksFromRemoteConfig = NetworkEnvironment.values().toList()
                            )
                        }
                        single { NetworkServicesUrlStorage(sharedPreferences = mockPrefs) }
                    }
                )
            )
        }
    }

    @Test
    fun test() = runTest {
//        Comment this cause request require VPN connection
//        interactor.loadPriceForTokens(
//            chain = TokenServiceNetwork.SOLANA,
//            tokenAddresses = listOf(
//                "So11111111111111111111111111111111111111112",
//                "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"
//            )
//        )
    }
}
