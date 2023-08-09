package org.p2p.wallet.infrastructure

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.crypto.Pbkdf2HashGenerator
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.dispatchers.DefaultDispatchers
import org.p2p.core.glide.GlideManager
import org.p2p.core.utils.namedByEnum
import org.p2p.core.utils.validators.BankingBicValidator
import org.p2p.core.utils.validators.BankingIbanValidator
import org.p2p.wallet.appsflyer.AppsFlyerService
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.intercom.IntercomDeeplinkManager
import org.p2p.wallet.intercom.IntercomPushService
import org.p2p.wallet.notification.AppNotificationManager
import org.p2p.wallet.push_notifications.repository.PushTokenRepository
import org.p2p.wallet.solana.SolanaNetworkObserver
import org.p2p.wallet.striga.signup.presetpicker.DefaultSelectItemSearcher
import org.p2p.wallet.striga.signup.presetpicker.SelectCountryProvider
import org.p2p.wallet.striga.signup.presetpicker.SelectItemContract
import org.p2p.wallet.striga.signup.presetpicker.SelectItemPresenter
import org.p2p.wallet.striga.signup.presetpicker.SelectItemPresenterCellMapper
import org.p2p.wallet.striga.signup.presetpicker.SelectItemProvider
import org.p2p.wallet.striga.signup.presetpicker.SelectItemProviderType
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
                ),
                socketEnabledFeatureToggle = get()
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
        factoryOf(::BankingIbanValidator)
        factoryOf(::BankingBicValidator)

        factoryOf(::DefaultSelectItemSearcher)
        factoryOf(::SelectItemPresenterCellMapper)
        factory { (provider: SelectItemProvider, selectedItemId: String?) ->
            SelectItemPresenter(
                provider = provider,
                cellMapper = get(),
                selectedItemId = selectedItemId,
                dispatchers = get()
            )
        } bind SelectItemContract.Presenter::class
        factoryOf(::SelectCountryProvider) {
            namedByEnum(SelectItemProviderType.SELECT_COUNTRY)
            bind<SelectItemProvider>()
        }

        includes(StorageModule.create(), RoomModule.create())
    }
}
