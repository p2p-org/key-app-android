package org.p2p.wallet.home.events

import org.koin.core.module.Module
import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.tokenservice.TokenServiceCoordinator
import org.p2p.wallet.updates.subscribe.SolanaAccountUpdateSubscriber
import org.p2p.wallet.updates.subscribe.SplTokenProgramSubscriber
import org.p2p.wallet.updates.subscribe.SubscriptionUpdateSubscriber
import org.p2p.wallet.updates.subscribe.Token2022ProgramSubscriber

object HomeEventsModule : InjectionModule {

    override fun create(): Module = module {
        singleOf(::SplTokenProgramSubscriber) bind SubscriptionUpdateSubscriber::class
        singleOf(::Token2022ProgramSubscriber) bind SubscriptionUpdateSubscriber::class
        singleOf(::SolanaAccountUpdateSubscriber) bind SubscriptionUpdateSubscriber::class

        singleOf(::OnboardingMetadataLoader) bind AppLoader::class
        singleOf(::PendingClaimBundlesLoader) bind AppLoader::class
        singleOf(::SolanaObservationLoader) bind AppLoader::class
        singleOf(::TokenServiceLoader) bind AppLoader::class

        single {
            StrigaFeatureLoader(
                strigaSignupEnabledFeatureToggle = get(),
                strigaUserInteractor = get(),
                strigaSignupInteractor = get(),
            ).apply {
                dependsOn(get<OnboardingMetadataLoader>())
            }
        } bind AppLoader::class

        single {
            SocketSubscribeLoader(
                updatesManager = get(),
                updateSubscribers = getAll(SubscriptionUpdateSubscriber::class),
            )
        } bind AppLoader::class

        single {
            AppLoaderFacade(
                appLoaders = getAll(AppLoader::class),
                appScope = get(),
            )
        }

        single {
            val solTokensLoader = new(::SolanaTokensLoader)
            val ethTokensLoader = new(::EthereumTokensLoader)

            TokenServiceCoordinator(
                solanaTokensLoader = solTokensLoader,
                ethereumTokensLoader = ethTokensLoader,
                appScope = get()
            )
        }
    }
}
