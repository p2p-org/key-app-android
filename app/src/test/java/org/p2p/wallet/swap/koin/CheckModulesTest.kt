package org.p2p.wallet.swap.koin

import android.R
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.koinApplication
import org.koin.test.KoinTest
import org.koin.test.check.checkModules
import org.koin.test.mock.MockProviderRule
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.p2p.wallet.AppModule
import org.p2p.wallet.auth.AuthModule
import org.p2p.wallet.common.analytics.AnalyticsModule
import org.p2p.wallet.common.crypto.keystore.EncoderDecoderMarshmallow
import org.p2p.wallet.common.crypto.keystore.KeyStoreWrapper
import org.p2p.wallet.feerelayer.FeeRelayerModule
import org.p2p.wallet.history.HistoryModule
import org.p2p.wallet.home.HomeModule
import org.p2p.wallet.infrastructure.InfrastructureModule
import org.p2p.wallet.infrastructure.network.NetworkModule
import org.p2p.wallet.qr.QrModule
import org.p2p.wallet.renbtc.RenBtcModule
import org.p2p.wallet.restore.BackupModule
import org.p2p.wallet.root.RootModule
import org.p2p.wallet.rpc.RpcModule
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.settings.SettingsModule
import org.p2p.wallet.swap.SwapModule
import org.p2p.wallet.transaction.di.TransactionModule
import org.p2p.wallet.user.UserModule

class CheckModulesTest : KoinTest {

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        println("MOCKING $clazz")
        Mockito.mock(clazz.java)
    }

    @Test
    fun verifyKoinApp() {
        koinApplication {
            modules(
                listOf(
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
                    AppModule.create(
                        application = mock(),
                        restartAction = {}
                    )
                )
            )
            checkModules() {
                withInstance<Context>()
                withInstance(mock<Context> { on { getString(anyInt()) }.doReturn("https://blockstream.info/") })
                withInstance(NetworkType.BITCOIN)
                withInstance<Application>()
                withInstance<SharedPreferences>()
            }
        }
    }
}
