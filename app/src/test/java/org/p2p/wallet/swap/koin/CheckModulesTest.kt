package org.p2p.wallet.swap.koin

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.getSystemService
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.ConnectivityManager
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
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
import org.mockito.Mockito
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
import org.p2p.wallet.infrastructure.security.SecureStorage
import org.p2p.wallet.push_notifications.PushNotificationsModule
import org.p2p.wallet.qr.ScanQrModule
import org.p2p.wallet.receive.network.ReceiveNetworkTypeContract
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
import java.io.File
import java.math.BigDecimal
import java.security.KeyStore
import javax.crypto.Cipher
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

    private val sharedPrefsMock: SharedPreferences = mockk(relaxed = true) {
        every { getString(eq("KEY_BASE_URL"), any()) }.returns(Environment.RPC_POOL.endpoint)
    }

    private val resourcesMock: Resources = mockk(relaxed = true)

    private val contextMock: Context = mockk(relaxed = true) {
        every { getSharedPreferences(any(), any()) }.returns(sharedPrefsMock)
        every { getString(any()) }.returns("https://blockstream.info/")
        every { resources }.returns(resourcesMock)
        every { theme }.returns(mockk())
        every { packageName }.returns("p2p.wallet")

        val connectivityManagerMock: ConnectivityManager = mockk {
            every { allNetworks }.returns(emptyArray())
        }
        every { getSystemService<ConnectivityManager>() }.returns(connectivityManagerMock)
        every { getSystemService<NotificationManager>() }.returns(mockk())

        val externalDirMock: File = mockk(relaxed = true) {
            every { path }.returns("somepath")
        }
        every { getExternalFilesDir(any()) }.returns(externalDirMock)
    }

    private val applicationMock: Application = mockk {
        every { applicationContext }.returns(contextMock)
        every { baseContext }.returns(contextMock)
        every { resources }.returns(resourcesMock)
    }

    private val javaxDefaultModule: Module = module {
        // no AndroidKeyStore found in unit tests, so override with default
        single<KeyStore> {
            mockk(relaxed = true) { every { getKey(any(), any()) }.returns(mockk()) }
        }
    }

    @Before
    fun before() {
        // need to fix The main looper is not available at AndroidDispatcherFactory
        // reason: runBlocking usage in some `init` cases of classes
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @Test
    fun verifyKoinApp() {
        mockFirebase()

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
                withInstance(mockk<SecureStorage>())

                withParameter<ReceiveNetworkTypePresenter> { NetworkType.BITCOIN }
                withParameter<ReceiveNetworkTypeContract.Presenter> { NetworkType.BITCOIN }
            }
        )
    }

    private fun mockFirebase() {
        mockkStatic(FirebaseApp::class, FirebaseCrashlytics::class)
        every { FirebaseApp.getInstance() } returns mockk(relaxed = true)
        every { FirebaseCrashlytics.getInstance() } returns mockk(relaxed = true)

        mockkStatic(Cipher::class)
        every { Cipher.getInstance(any()) } returns mockk(relaxed = true)
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
        ScanQrModule.create(),
        HistoryModule.create(),
        SettingsModule.create(),
        SwapModule.create(),
        RpcModule.create(),
        FeeRelayerModule.create(),
        InfrastructureModule.create(),
        PushNotificationsModule.create(),
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
