package org.p2p.wallet.infrastructure

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.crypto.Pbkdf2HashGenerator
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.dispatchers.DefaultDispatchers
import org.p2p.core.glide.GlideManager
import org.p2p.core.utils.validators.BankingBicValidator
import org.p2p.core.utils.validators.BankingIbanValidator
import org.p2p.solanaj.utils.SolanaMessageSigner
import org.p2p.wallet.appsflyer.AppsFlyerService
import org.p2p.wallet.deeplinks.DeeplinkModule
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.intercom.IntercomPushService
import org.p2p.wallet.notification.AppNotificationManager
import org.p2p.wallet.push_notifications.repository.PushTokenRepository
import org.p2p.wallet.solana.SolanaNetworkObserver
import org.p2p.wallet.utils.UsernameFormatter

object InfrastructureModule : InjectionModule {

    override fun create() = module {
        singleOf(::GlideManager)

        singleOf(::AppNotificationManager)

        singleOf(::DefaultDispatchers) bind CoroutineDispatchers::class

        singleOf(::PushTokenRepository)

        factoryOf(::Pbkdf2HashGenerator)
        singleOf(::AppsFlyerService)

        singleOf(::SolanaNetworkObserver)

        singleOf(::IntercomPushService)

        single { TokenConverter }

        singleOf(::UsernameFormatter)
        factoryOf(::BankingIbanValidator)
        factoryOf(::BankingBicValidator)

        factoryOf(::SolanaMessageSigner)

        includes(StorageModule.create(), RoomModule.create(), SocketsModule.create(), DeeplinkModule.create())
    }
}
