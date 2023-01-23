package org.p2p.wallet.swap.koin

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.getSystemService
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.ConnectivityManager
import android.webkit.WebView
import com.appsflyer.AppsFlyerLib
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.koin.core.module.Module
import org.koin.core.parameter.ParametersHolder
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.check.ParametersBinding
import org.koin.test.check.checkKoinModules
import org.koin.test.mock.MockProviderRule
import org.p2p.core.token.Token
import org.p2p.core.token.TokenVisibility
import org.p2p.wallet.AppModule
import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.auth.ui.generalerror.OnboardingGeneralErrorPresenter
import org.p2p.wallet.auth.ui.generalerror.timer.GeneralErrorTimerScreenError
import org.p2p.wallet.auth.ui.generalerror.timer.OnboardingGeneralErrorTimerPresenter
import org.p2p.wallet.auth.ui.restore_error.RestoreErrorScreenPresenter
import org.p2p.wallet.history.model.TransactionDetailsLaunchState
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironment
import org.p2p.wallet.infrastructure.security.SecureStorage
import org.p2p.wallet.infrastructure.transactionmanager.impl.TransactionWorker
import org.p2p.wallet.newsend.ui.NewSendPresenter
import org.p2p.wallet.receive.network.ReceiveNetworkTypeContract
import org.p2p.wallet.receive.network.ReceiveNetworkTypePresenter
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import org.p2p.wallet.send.model.AddressState
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.solend.model.SolendDepositToken
import org.robolectric.fakes.RoboWebSettings
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
        mockkClass(clazz)
    }

    private val logger: Logger = object : Logger() {
        override fun log(level: Level, msg: MESSAGE) {
            println("KOIN (${level.name}): $msg")
        }
    }

    private val sharedPrefsMock: SharedPreferences = mockk(relaxed = true) {
        every { getString(eq("KEY_BASE_URL"), any()) }.returns(NetworkEnvironment.RPC_POOL.endpoint)
        every { getString(eq("KEY_FEE_RELAYER_BASE_URL"), any()) }
            .returns("https://test-solana-fee-relayer.wallet.p2p.org/")
        every { getString(eq("KEY_NAME_SERVICE_BASE_URL"), any()) }
            .returns("https://name-register.key.app/")
        every { getString(eq("KEY_NOTIFICATION_SERVICE_BASE_URL"), any()) }
            .returns("http://35.234.120.240:9090/")
        every { getString(eq("KEY_MOONPAY_SERVER_SIDE_BASE_URL"), any()) }
            .returns("http://example.com")
        every { getString(eq("KEY_DEPOSIT_TICKER_BALANCE"), any()) }
            .returns("0")
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
        every { packageName }.returns("org.p2p.wallet")
    }

    private val javaxDefaultModule: Module = module {
        // no AndroidKeyStore found in unit tests, so override with default
        single<KeyStore> {
            mockk(relaxed = true) {
                every { getKey(any(), any()) }.returns(mockk())
            }
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

        mockkConstructor(WebView::class)
        every { anyConstructed<WebView>().settings }.returns(RoboWebSettings())

        mockSystemCalls()

        checkKoinModules(
            modules = AppModule.create(restartAction = {}) + javaxDefaultModule,
            appDeclaration = {
                allowOverride(override = true)

                logger(logger)
                androidContext(applicationMock)
                androidContext(contextMock)
            },
            parameters = {
                initParameters()
                initInstances()
            }
        )
    }

    private fun mockFirebase() {
        mockkStatic(FirebaseApp::class, FirebaseCrashlytics::class, FirebaseRemoteConfig::class, FirebaseAnalytics::class, AppsFlyerLib::class)
        every { FirebaseApp.getInstance() } returns mockk(relaxed = true)
        every { FirebaseCrashlytics.getInstance() } returns mockk(relaxed = true)
        every { FirebaseRemoteConfig.getInstance() } returns mockk(relaxed = true)
        every { FirebaseAnalytics.getInstance(applicationMock) } returns mockk(relaxed = true)
        every { AppsFlyerLib.getInstance() } returns mockk(relaxed = true)
        mockkStatic(Cipher::class)
        every { Cipher.getInstance(any()) } returns mockk(relaxed = true)
    }

    private fun mockSystemCalls() {
        mockkStatic(System::class)
        // mock native .so libs loading
        every { System.loadLibrary(any()) } returns mockk(relaxed = true)
    }

    private fun ParametersBinding.initParameters() {
        withParameter<NewSendPresenter> {
            SearchResult.UsernameFound(
                addressState = AddressState("3UxBMjZtMJVN4eub6a6hNvVe9bThxVg2s4zjNx2UML3b"),
                username = "chingiz.key"
            )
        }
        withParameter<ReceiveNetworkTypePresenter> {
            NetworkType.BITCOIN
        }
        withParameter<ReceiveNetworkTypeContract.Presenter> {
            NetworkType.BITCOIN
        }
        withParameter<OnboardingGeneralErrorPresenter> {
            GatewayHandledState.ToastError("Test message")
        }
        withParameters<OnboardingGeneralErrorTimerPresenter> {
            ParametersHolder(mutableListOf(GeneralErrorTimerScreenError.BLOCK_PHONE_NUMBER_ENTER, 10))
        }
        withParameter<RestoreErrorScreenPresenter> {
            RestoreFailureState.TitleSubtitleError(
                title = "Test",
                subtitle = "Test"
            )
        }
    }

    private fun ParametersBinding.initInstances() {
        withInstance(sharedPrefsMock)
        withInstance(mockk<Token.Active>())
        withInstance(mockk<SolendDepositToken.Active>())
        withInstance(mockk<SecureStorage>())
        withInstance(mockk<TransactionWorker>())
        withInstance(mockk<TransactionDetailsLaunchState.Id>())
        withInstance(mockk<SellTransactionViewDetails>())
    }

    @After
    fun after() {
        Dispatchers.resetMain()
    }

    private fun createEmptyActiveToken(): Token.Active {
        return Token.Active(
            publicKey = "",
            totalInUsd = null,
            total = BigDecimal.ZERO,
            visibility = TokenVisibility.DEFAULT,
            rate = null,
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

    private fun createEmptySolendDepositToken(): SolendDepositToken.Active {
        return SolendDepositToken.Active(
            tokenSymbol = "",
            tokenName = "",
            decimals = 6,
            mintAddress = "some address",
            iconUrl = null,
            availableTokensForDeposit = BigDecimal.TEN,
            supplyInterest = BigDecimal.ZERO,
            depositAmount = BigDecimal.ZERO,
            usdAmount = BigDecimal.ZERO,
            usdRate = BigDecimal.ZERO
        )
    }
}
