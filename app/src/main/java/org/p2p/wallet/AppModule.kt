package org.p2p.wallet

import android.content.res.Resources
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.p2p.wallet.auth.AuthModule
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.analytics.AnalyticsModule
import org.p2p.wallet.common.crashlogging.CrashLogger
import org.p2p.wallet.common.crashlogging.impl.FirebaseCrashlyticsFacade
import org.p2p.wallet.common.crashlogging.impl.SentryFacade
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.di.ServiceScope
import org.p2p.wallet.common.feature_toggles.di.FeatureTogglesModule
import org.p2p.wallet.debug.DebugSettingsModule
import org.p2p.wallet.feerelayer.FeeRelayerModule
import org.p2p.wallet.history.HistoryModule
import org.p2p.wallet.history.HistoryStrategyModule
import org.p2p.wallet.home.HomeModule
import org.p2p.wallet.infrastructure.InfrastructureModule
import org.p2p.wallet.infrastructure.network.NetworkModule
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManagerModule
import org.p2p.wallet.moonpay.BuyModule
import org.p2p.wallet.push_notifications.PushNotificationsModule
import org.p2p.wallet.qr.ScanQrModule
import org.p2p.wallet.renbtc.RenBtcModule
import org.p2p.wallet.restore.BackupModule
import org.p2p.wallet.root.RootModule
import org.p2p.wallet.rpc.RpcModule
import org.p2p.wallet.settings.SettingsModule
import org.p2p.wallet.swap.SwapModule
import org.p2p.wallet.transaction.di.TransactionModule
import org.p2p.wallet.user.UserModule
import org.p2p.wallet.user.repository.prices.di.TokenPricesModule
import org.p2p.wallet.sdk.di.P2PSdkModule

object AppModule {
    fun create(restartAction: () -> Unit) = module {
        singleOf(::AppScope)
        single<Resources> { androidContext().resources }
        singleOf(::InAppFeatureFlags)
        singleOf(::ResourcesProvider)
        singleOf(::ServiceScope)
        single { AppRestarter { restartAction.invoke() } }
        single {
            CrashLogger(
                crashLoggingFacades = listOf(
                    FirebaseCrashlyticsFacade(isFacadeEnabled = BuildConfig.CRASHLYTICS_ENABLED),
                    SentryFacade()
                ),
                tokenKeyProvider = get()
            )
        }

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
                P2PSdkModule.create(),

                // feature screens
                AuthModule.create(),
                RootModule.create(),
                PushNotificationsModule.create(),
                BackupModule.create(),
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
                HistoryStrategyModule.create(),
                TransactionManagerModule.create()
            )
        )
    }
}
