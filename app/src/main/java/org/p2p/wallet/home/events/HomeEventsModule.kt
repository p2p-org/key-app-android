package org.p2p.wallet.home.events

import org.koin.core.module.Module
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule

object HomeEventsModule : InjectionModule {

    override fun create(): Module = module {
        single {
            listOf(
                ActionButtonsLoader(homeInteractor = get(), sellEnabledFeatureToggle = get()),
                EthereumKitLoader(
                    seedPhraseProvider = get(),
                    bridgeFeatureToggle = get(),
                    ethereumInteractor = get(),
                    appScope = get(),
                    dispatchers = get()
                ),
                HomeScreenStateLoader(
                    userInteractor = get(),
                    ethereumInteractor = get(),
                    strigaClaimInteractor = get(),
                    homeInteractor = get()
                ),
                MetadataLoader(metadataInteractor = get()),
                PendingClaimBundlesLoader(bridgeLocalRepository = get(), appScope = get(), transactionManager = get()),
                SocketSubscribeLoader(
                    updatesManager = get(),
                    updateSubscribers = get(),
                    connectionManager = get(),
                    appScope = get()
                ),
                SolanaObservationLoader(networkObserver = get(), appScope = get()),
                SolanaTokensLoader(
                    homeInteractor = get(),
                    appScope = get(),
                    tokenKeyProvider = get(),
                    connectionManager = get(),
                    environmentManager = get()
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
        }
    }
}
