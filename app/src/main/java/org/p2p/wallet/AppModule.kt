package org.p2p.wallet

import android.content.res.Resources
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.p2p.core.common.di.AppScope
import org.p2p.core.common.di.ServiceScope
import org.p2p.core.crashlytics.CrashLoggerModule
import org.p2p.core.network.ConnectionManager
import org.p2p.core.network.NetworkCoreModule
import org.p2p.wallet.alarmlogger.AlarmErrorsModule
import org.p2p.wallet.auth.AuthModule
import org.p2p.wallet.bridge.BridgeModule
import org.p2p.wallet.bridge.claim.ClaimModule
import org.p2p.wallet.bridge.send.BridgeSendModule
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.analytics.AnalyticsModule
import org.p2p.wallet.common.feature_toggles.di.FeatureTogglesModule
import org.p2p.wallet.debug.DebugSettingsModule
import org.p2p.wallet.feerelayer.FeeRelayerModule
import org.p2p.wallet.history.HistoryModule
import org.p2p.wallet.home.HomeModule
import org.p2p.wallet.infrastructure.InfrastructureModule
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.ethereumkit.external.EthereumModule
import org.p2p.token.service.TokenServiceModule
import org.p2p.wallet.home.events.HomeEventsModule
import org.p2p.wallet.infrastructure.network.NetworkModule
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManagerModule
import org.p2p.wallet.jupiter.JupiterModule
import org.p2p.wallet.moonpay.MoonpayModule
import org.p2p.wallet.moonpay.ui.BuyModule
import org.p2p.wallet.newsend.SendModule
import org.p2p.wallet.push_notifications.PushNotificationsModule
import org.p2p.wallet.qr.ScanQrModule
import org.p2p.wallet.receive.ReceiveModule
import org.p2p.wallet.renbtc.RenBtcModule
import org.p2p.wallet.restore.RestoreModule
import org.p2p.wallet.root.RootModule
import org.p2p.wallet.rpc.RpcModule
import org.p2p.wallet.sdk.di.AppSdkModule
import org.p2p.wallet.sell.SellModule
import org.p2p.wallet.settings.SettingsModule
import org.p2p.wallet.solend.SolendModule
import org.p2p.wallet.striga.StrigaModule
import org.p2p.wallet.swap.SwapModule
import org.p2p.wallet.transaction.di.TransactionModule
import org.p2p.wallet.user.UserModule

object AppModule {
    fun create(restartAction: () -> Unit) = module {
        singleOf(::AppScope)
        single<Resources> { androidContext().resources }
        singleOf(::ServiceScope)
        single { AppRestarter { restartAction.invoke() } }
        single {
            ConnectionManager(
                context = androidContext(),
                scope = get<AppScope>(),
                checkInetDispatcher = get<CoroutineDispatchers>().io
            )
        }

        singleOf(::AppCreatedAction)

        includes(
            listOf(
                // core modules
                AlarmErrorsModule.create(),
                AnalyticsModule.create(),
                AppSdkModule.create(),
                FeeRelayerModule.create(),
                FeatureTogglesModule.create(),
                InfrastructureModule.create(),
                MoonpayModule.create(),
                NetworkModule.create(),
                RpcModule.create(),
                TransactionModule.create(),
                // feature screens
                AuthModule.create(),
                BridgeModule.create(),
                BridgeSendModule.create(),
                BuyModule.create(),
                ClaimModule.create(),
                DebugSettingsModule.create(),
                HistoryModule.create(),
                HomeModule.create(),
                JupiterModule.create(),
                PushNotificationsModule.create(),
                ReceiveModule.create(),
                RenBtcModule.create(),
                RestoreModule.create(),
                RootModule.create(),
                ScanQrModule.create(),
                SendModule.create(),
                SellModule.create(),
                SettingsModule.create(),
                SolendModule.create(),
                StrigaModule.create(),
                SwapModule.create(),
                TransactionManagerModule.create(),
                UserModule.create(),
                CrashLoggerModule.create(),
                NetworkCoreModule.create(),
                UserModule.create(),
                CrashLoggerModule.create(),
                NetworkCoreModule.create(),
                TokenServiceModule.create(),
                HomeEventsModule.create(),
                EthereumModule.create()
            )
        )
    }
}
