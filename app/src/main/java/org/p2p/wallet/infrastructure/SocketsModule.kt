package org.p2p.wallet.infrastructure

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.updates.SocketUpdatesManager
import org.p2p.wallet.updates.SubscriptionUpdatesManager
import org.p2p.wallet.updates.handler.SolanaAccountUpdateHandler
import org.p2p.wallet.updates.handler.SplTokenProgramUpdateHandler
import org.p2p.wallet.updates.handler.TransactionSignatureHandler

object SocketsModule : InjectionModule {

    override fun create() = module {
        singleOf(::TransactionSignatureHandler)
        singleOf(::SolanaAccountUpdateHandler)
        singleOf(::SplTokenProgramUpdateHandler)
        single {
            val updateHandlers = listOf(
                get<TransactionSignatureHandler>(),
                get<SolanaAccountUpdateHandler>(),
                get<SplTokenProgramUpdateHandler>(),
            )

            SocketUpdatesManager(
                appScope = get(),
                environmentManager = get(),
                connectionStateProvider = get(),
                updateHandlers = updateHandlers,
                socketEnabledFeatureToggle = get()
            )
        } bind SubscriptionUpdatesManager::class
    }
}
