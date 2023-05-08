package org.p2p.wallet.infrastructure

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.glide.GlideManager
import org.p2p.solanaj.utils.crypto.Pbkdf2HashGenerator
import org.p2p.wallet.appsflyer.AppsFlyerService
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.dispatchers.DefaultDispatchers
import org.p2p.wallet.intercom.IntercomDeeplinkManager
import org.p2p.wallet.intercom.IntercomPushService
import org.p2p.wallet.notification.AppNotificationManager
import org.p2p.wallet.push_notifications.repository.PushTokenRepository
import org.p2p.wallet.solana.SolanaNetworkObserver
import org.p2p.wallet.updates.SocketUpdatesManager
import org.p2p.wallet.updates.SubscriptionUpdatesManager
import org.p2p.wallet.updates.handler.SolanaAccountUpdateHandler
import org.p2p.wallet.updates.handler.SplTokenProgramUpdateHandler
import org.p2p.wallet.updates.handler.TransactionSignatureHandler
import org.p2p.wallet.utils.UsernameFormatter

object InfrastructureModule : InjectionModule {

    override fun create() = module {
        singleOf(::GlideManager)

        singleOf(::TransactionSignatureHandler)
        singleOf(::SolanaAccountUpdateHandler)
        singleOf(::SplTokenProgramUpdateHandler)
        single {
            SocketUpdatesManager(
                appScope = get(),
                environmentManager = get(),
                connectionStateProvider = get(),
                updateHandlers = listOf(
                    get<TransactionSignatureHandler>(),
                    get<SolanaAccountUpdateHandler>(),
                    get<SplTokenProgramUpdateHandler>(),
                )
            )
        } bind SubscriptionUpdatesManager::class

        singleOf(::AppDeeplinksManager)
        singleOf(::AppNotificationManager)

        singleOf(::DefaultDispatchers) bind CoroutineDispatchers::class

        singleOf(::PushTokenRepository)

        factoryOf(::Pbkdf2HashGenerator)
        singleOf(::AppsFlyerService)

        singleOf(::SolanaNetworkObserver)

        singleOf(::IntercomPushService)
        singleOf(::IntercomDeeplinkManager)
        single { TokenConverter }

        singleOf(::UsernameFormatter)

        includes(StorageModule.create(), RoomModule.create())
    }
}
