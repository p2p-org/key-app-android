package org.p2p.wallet.home.events

import org.koin.core.module.Module
import org.koin.core.module.dsl.new
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.bridge.EthereumTokensPollingService
import org.p2p.wallet.tokenservice.TokenService
import org.p2p.wallet.tokenservice.TokenServiceLoadStateHelper
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
                ActionButtonsLoader(
                    homeInteractor = get(),
                    sellEnabledFeatureToggle = get()
                ),
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
                SocketSubscribeLoader(
                    updatesManager = get(),
                    updateSubscribers = subscribers,
                    connectionManager = get(),
                    appScope = get()
                ),
                SolanaObservationLoader(
                    networkObserver = get(),
                    appScope = get()
                ),
                StrigaBannersLoader(
                    strigaUserInteractor = get(),
                    strigaSignupEnabledFeatureToggle = get(),
                    appScope = get(),
                    homeInteractor = get()
                ),
                StrigaFeatureLoader(
                    strigaSignupEnabledFeatureToggle = get(),
                    strigaUserInteractor = get(),
                    strigaSignupInteractor = get(),
                    strigaWalletInteractor = get()
                ),
                UsernameLoader(
                    homeInteractor = get(),
                    tokenKeyProvider = get()
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

            TokenService(
                userTokensInteractor = get(),
                ethereumInteractor = get(),
                solanaTokensLoader = solTokensLoader,
                ethereumTokensLoader = ethTokensLoader,
                appScope = get(),
                loadStateHelper = TokenServiceLoadStateHelper
            )
        }
    }
}
