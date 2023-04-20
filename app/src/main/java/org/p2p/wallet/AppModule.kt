package org.p2p.wallet

import android.content.res.Resources
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.p2p.ethereumkit.EthereumKitService
import org.p2p.wallet.auth.AuthModule
import org.p2p.wallet.bridge.BridgeModule
import org.p2p.wallet.bridge.claim.ClaimModule
import org.p2p.wallet.bridge.send.BridgeSendModule
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.analytics.AnalyticsModule
import org.p2p.wallet.common.crashlogging.CrashLogger
import org.p2p.wallet.common.crashlogging.CrashLoggingFacade
import org.p2p.wallet.common.crashlogging.impl.FirebaseCrashlyticsFacade
import org.p2p.wallet.common.crashlogging.impl.SentryFacade
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.di.ServiceScope
import org.p2p.wallet.common.feature_toggles.di.FeatureTogglesModule
import org.p2p.wallet.debug.DebugSettingsModule
import org.p2p.wallet.feerelayer.FeeRelayerModule
import org.p2p.wallet.history.HistoryModule
import org.p2p.wallet.home.HomeModule
import org.p2p.wallet.infrastructure.InfrastructureModule
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
import org.p2p.wallet.swap.SwapModule
import org.p2p.wallet.transaction.di.TransactionModule
import org.p2p.wallet.user.UserModule
import org.p2p.wallet.user.repository.prices.di.TokenPricesModule

object AppModule {
    fun create(restartAction: () -> Unit) = module {
        singleOf(::AppScope)
        single<Resources> { androidContext().resources }
        singleOf(::InAppFeatureFlags)
        singleOf(::ServiceScope)
        single { AppRestarter { restartAction.invoke() } }
        single {
            CrashLogger(
                crashLoggingFacades = getActiveCrashLoggingFacades(),
                tokenKeyProvider = get()
            )
        }

        singleOf(::AppCreatedAction)

        includes(
            listOf(
                // core modules
                NetworkModule.create(),
                RpcModule.create(),
                FeeRelayerModule.create(),
                InfrastructureModule.create(),
                TransactionModule.create(),
                AnalyticsModule.create(),
                FeatureTogglesModule.create(),
                AppSdkModule.create(),
                MoonpayModule.create(),

                // feature screens
                AuthModule.create(),
                RootModule.create(),
                PushNotificationsModule.create(),
                RestoreModule.create(),
                UserModule.create(),
                TokenPricesModule.create(),
                HomeModule.create(),
                BuyModule.create(),
                RenBtcModule.create(),
                ScanQrModule.create(),
                HistoryModule.create(),
                SettingsModule.create(),
                DebugSettingsModule.create(),
                SwapModule.create(),
                SendModule.create(),
                TransactionManagerModule.create(),
                SolendModule.create(),
                SellModule.create(),
                JupiterModule.create(),
                ReceiveModule.create(),
                BridgeSendModule.create(),
                ClaimModule.create(),
                BridgeModule.create()
            ) + EthereumKitService.getEthereumKitModules()
        )
    }

    private fun getActiveCrashLoggingFacades(): List<CrashLoggingFacade> = buildList {
        if (BuildConfig.CRASHLYTICS_ENABLED) {
            add(FirebaseCrashlyticsFacade())
        }
        if (BuildConfig.SENTRY_ENABLED) {
            add(SentryFacade())
        }
    }
}
