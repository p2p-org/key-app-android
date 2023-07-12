package org.p2p.wallet.home.events

import org.koin.core.module.Module
import org.koin.core.module.dsl.new
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.bridge.EthereumTokensPollingService
import org.p2p.wallet.tokenservice.TokenServiceCoordinator
import org.p2p.wallet.updates.subscribe.SolanaAccountUpdateSubscriber
import org.p2p.wallet.updates.subscribe.SplTokenProgramSubscriber

object HomeEventsModule : InjectionModule {

    override fun create(): Module = module {
        single<AppLoader> {
            val subscribers = listOf(
                new(::SplTokenProgramSubscriber),
                new(::SolanaAccountUpdateSubscriber)
            )
            val appLoaders = listOf(
                OnboardingMetadataLoader(
                    metadataInteractor = get()
                ),
                PendingClaimBundlesLoader(
                    bridgeLocalRepository = get(),
                    appScope = get(),
                    transactionManager = get(),
                    seedPhraseProvider = get(),
                    bridgeFeatureToggle = get()
                ),
//                SocketSubscribeLoader(
//                    updatesManager = get(),
//                    updateSubscribers = subscribers
//                ),
                SolanaObservationLoader(
                    networkObserver = get(),
                    appScope = get()
                ),
                StrigaFeatureLoader(
                    strigaSignupEnabledFeatureToggle = get(),
                    strigaUserInteractor = get(),
                    strigaSignupInteractor = get(),
                    strigaWalletInteractor = get()
                )

            )
            AppLoaderFacade(appLoaders, appScope = get())
        }
        single {
            EthereumTokensPollingService(
                ethereumInteractor = get(),
                appScope = get()
            )
        }

        single {
            val solTokensLoader = SolanaTokensLoader(
                userTokensInteractor = get(),
                tokenKeyProvider = get(),
                tokenServiceEventManager = get(),
                appScope = get()
            )
            val ethTokensLoader = EthereumTokensLoader(
                seedPhraseProvider = get(),
                bridgeFeatureToggle = get(),
                ethereumInteractor = get(),
                ethereumTokensPollingService = get(),
                tokenServiceEventManager = get(),
                tokenServiceEventPublisher = get(),
                appScope = get()
            )

            TokenServiceCoordinator(
                solanaTokensLoader = solTokensLoader,
                ethereumTokensLoader = ethTokensLoader,
                appScope = get()
            )
        }
    }
}
