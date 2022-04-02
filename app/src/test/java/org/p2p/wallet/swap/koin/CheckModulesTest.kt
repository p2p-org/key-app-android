package org.p2p.wallet.swap.koin

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.getSystemService
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.check.checkKoinModules
import org.koin.test.mock.MockProviderRule
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.p2p.solanaj.rpc.Environment
import org.p2p.wallet.AppModule
import org.p2p.wallet.auth.AuthModule
import org.p2p.wallet.common.analytics.AnalyticsModule
import org.p2p.wallet.feerelayer.FeeRelayerModule
import org.p2p.wallet.history.HistoryModule
import org.p2p.wallet.home.HomeModule
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.model.TokenVisibility
import org.p2p.wallet.infrastructure.InfrastructureModule
import org.p2p.wallet.infrastructure.network.NetworkModule
import org.p2p.wallet.qr.QrModule
import org.p2p.wallet.receive.network.ReceiveNetworkTypePresenter
import org.p2p.wallet.renbtc.RenBtcModule
import org.p2p.wallet.restore.BackupModule
import org.p2p.wallet.root.RootModule
import org.p2p.wallet.rpc.RpcModule
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.settings.SettingsModule
import org.p2p.wallet.swap.SwapModule
import org.p2p.wallet.transaction.di.TransactionModule
import org.p2p.wallet.user.UserModule
import java.math.BigDecimal
import java.security.KeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@ExperimentalCoroutinesApi
class CheckModulesTest : KoinTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        println("MOCKING $clazz")
        Mockito.mock(clazz.java)
    }

    private val sharedPrefsMock: SharedPreferences = mock {
        on { getString(eq("KEY_BASE_URL"), anyString()) }.thenReturn(Environment.RPC_POOL.endpoint)
    }

    private val contextMock: Context = mock {
        on { getSharedPreferences(anyString(), anyInt()) }.doReturn(sharedPrefsMock)
        on { getString(anyInt()) }.doReturn("https://blockstream.info/")
        on { resources }.doReturn(mock())

        val connectivityManagerMock = mock<ConnectivityManager> { on { allNetworks }.thenReturn(emptyArray()) }
        on { getSystemService<ConnectivityManager>() }.thenReturn(connectivityManagerMock)
        on { getSystemService<NotificationManager>() }.thenReturn(mock())
    }

    private val applicationMock: Application = mock {
        on { applicationContext }.thenReturn(contextMock)
        on { baseContext }.thenReturn(contextMock)
    }

    private val javaxDefaultModule: Module = module {
        // no AndroidKeyStore found in unit tests, so override with default
        single { KeyStore.getInstance(KeyStore.getDefaultType()) }
    }

    @Before
    fun before() {
        // need to fix The main looper is not available at AndroidDispatcherFactory
        // reason: runBlocking usage in some `init` cases of classes
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @Test
    fun verifyKoinApp() {
        checkKoinModules(
            modules = allModules + javaxDefaultModule,
            appDeclaration = {
                allowOverride(override = true)

                androidContext(applicationMock)
                androidContext(contextMock)
            },
            parameters = {
                withInstance(sharedPrefsMock)
                withInstance(createEmptyActiveToken())

                withParameter<ReceiveNetworkTypePresenter> { NetworkType.BITCOIN }
            }
        )
    }

    @After
    fun after() {
        Dispatchers.resetMain()
    }

    private val allModules = listOf(
        AuthModule.create(),
        RootModule.create(),
        BackupModule.create(),
        UserModule.create(),
        HomeModule.create(),
        RenBtcModule.create(),
        NetworkModule.create(),
        QrModule.create(),
        HistoryModule.create(),
        SettingsModule.create(),
        SwapModule.create(),
        RpcModule.create(),
        FeeRelayerModule.create(),
        InfrastructureModule.create(),
        TransactionModule.create(),
        AnalyticsModule.create(),
        AppModule.create(restartAction = {})
    )

    private fun createEmptyActiveToken(): Token.Active {
        return Token.Active(
            publicKey = "",
            totalInUsd = null,
            total = BigDecimal.ZERO,
            visibility = TokenVisibility.DEFAULT,
            usdRate = null,
            tokenSymbol = "",
            decimals = 0,
            mintAddress = "",
            tokenName = "",
            iconUrl = null,
            serumV3Usdc = null,
            serumV3Usdt = null,
            isWrapped = false
        )
    }
}
