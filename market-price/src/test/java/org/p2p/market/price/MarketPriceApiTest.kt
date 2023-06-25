package org.p2p.market.price

import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import android.content.SharedPreferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
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
import kotlin.math.sin
import kotlinx.coroutines.test.runTest
import org.p2p.core.R
import org.p2p.core.crashlytics.CrashLoggerModule
import org.p2p.core.network.NetworkCoreModule
import org.p2p.core.network.NetworkServicesUrlStorage
import org.p2p.core.network.environment.NetworkEnvironment
import org.p2p.core.network.environment.NetworkEnvironmentManager
import org.p2p.market.price.api.request.MarketPriceItemRequest
import org.p2p.market.price.api.request.MarketPriceRequest
import org.p2p.market.price.api.response.NetworkChain
import org.p2p.market.price.mock.NetworkEnvironmentStorageMock
import org.p2p.market.price.repository.MarketPriceRepository

@RunWith(AndroidJUnit4::class)
class MarketPriceApiTest : KoinTest {

    private val repository: MarketPriceRepository by inject()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        val context = mockk<Context>() {
            coEvery { getString(R.string.bridgesServiceBaseUrl) } returns "https://bridge-service.key.app/"
        }
        val mockPRefs = mockk<SharedPreferences>() {
            coEvery { getString("KEY_BRIDGES_SERVICE_BASE_URL","https://bridge-service.key.app/") } returns "https://bridge-service.key.app/"
        }
        startKoin {
            androidContext(context)
            loadKoinModules(
                listOf(
                    NetworkCoreModule.create(),
                    CrashLoggerModule.create(),
                    MarketPriceModule.create(),
                    module {
                        single {
                            NetworkEnvironmentManager(
                                networkEnvironmentStorage = NetworkEnvironmentStorageMock(),
                                crashLogger = get(),
                                networksFromRemoteConfig = NetworkEnvironment.values().toList()
                            )
                        }
                        single { NetworkServicesUrlStorage(sharedPreferences = mockPRefs) }

                    }
                )
            )
        }
    }

    @Test
    fun test() = runTest {

        repository.launch(
            MarketPriceRequest(
                marketRequest = listOf(
                    MarketPriceItemRequest(
                        chainId = NetworkChain.SOLANA,
                        addresses = listOf(
                            "So11111111111111111111111111111111111111112",
                            "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"
                        )
                    )
                )
            )
        )
    }
}
