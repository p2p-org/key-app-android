package org.p2p.wallet.home

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.repository.HomeDatabaseRepository
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.home.ui.main.HomeContract
import org.p2p.wallet.home.ui.main.HomeElementItemMapper
import org.p2p.wallet.home.ui.main.HomePresenter
import org.p2p.wallet.home.ui.select.SelectTokenContract
import org.p2p.wallet.home.ui.select.SelectTokenPresenter
import org.p2p.wallet.moonpay.api.MoonpayApi
import org.p2p.wallet.moonpay.repository.MoonpayApiMapper
import org.p2p.wallet.moonpay.repository.MoonpayRemoteRepository
import org.p2p.wallet.moonpay.repository.MoonpayRepository
import org.p2p.wallet.moonpay.ui.BuySolanaContract
import org.p2p.wallet.moonpay.ui.BuySolanaPresenter
import org.p2p.wallet.receive.list.TokenListContract
import org.p2p.wallet.receive.list.TokenListPresenter
import org.p2p.wallet.receive.network.ReceiveNetworkTypeContract
import org.p2p.wallet.receive.network.ReceiveNetworkTypePresenter
import org.p2p.wallet.receive.renbtc.ReceiveRenBtcContract
import org.p2p.wallet.receive.renbtc.ReceiveRenBtcPresenter
import org.p2p.wallet.receive.solana.ReceiveSolanaContract
import org.p2p.wallet.receive.solana.ReceiveSolanaPresenter
import org.p2p.wallet.receive.token.ReceiveTokenContract
import org.p2p.wallet.receive.token.ReceiveTokenPresenter
import org.p2p.wallet.send.interactor.SearchInteractor
import org.p2p.wallet.send.interactor.SendInteractor
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.ui.main.SendContract
import org.p2p.wallet.send.ui.main.SendPresenter
import org.p2p.wallet.send.ui.search.SearchContract
import org.p2p.wallet.send.ui.search.SearchPresenter
import retrofit2.Retrofit

object HomeModule : InjectionModule {

    const val MOONPAY_QUALIFIER = "api.moonpay.com"

    override fun create() = module {
        initDataLayer()
        initDomainLayer()
        initPresentationLayer()
    }

    private fun Module.initDataLayer() {
        factory { MoonpayApiMapper() }
        factory<MoonpayRepository> {
            val api = get<Retrofit>(named(MOONPAY_QUALIFIER)).create(MoonpayApi::class.java)
            val apiKey = BuildConfig.moonpayKey
            MoonpayRemoteRepository(api, apiKey, get())
        }

        factory<HomeLocalRepository> { HomeDatabaseRepository(get()) }
    }

    private fun Module.initDomainLayer() {
        single {
            SendInteractor(
                addressInteractor = get(),
                feeRelayerInteractor = get(),
                feeRelayerAccountInteractor = get(),
                feeRelayerTopUpInteractor = get(),
                orcaInfoInteractor = get(),
                amountRepository = get(),
                transactionInteractor = get(),
                tokenKeyProvider = get(),
            )
        }
        factory {
            SearchInteractor(
                usernameInteractor = get(),
                userInteractor = get(),
                tokenKeyProvider = get()
            )
        }
    }

    private fun Module.initPresentationLayer() {
        /* Cached data exists, therefore creating singleton */
        factory<HomeContract.Presenter> {
            HomePresenter(
                inAppFeatureFlags = get(),
                updatesManager = get(),
                userInteractor = get(),
                settingsInteractor = get(),
                usernameInteractor = get(),
                environmentManager = get(),
                tokenKeyProvider = get(),
                homeElementItemMapper = HomeElementItemMapper(),
                resourcesProvider = get()
            )
        }

        factory<ReceiveSolanaContract.Presenter> { (token: Token.Active?) ->
            ReceiveSolanaPresenter(
                defaultToken = token,
                userInteractor = get(),
                qrCodeInteractor = get(),
                usernameInteractor = get(),
                tokenKeyProvider = get(),
                receiveAnalytics = get(),
                context = get()
            )
        }
        factory<ReceiveNetworkTypeContract.Presenter> { (type: NetworkType) ->
            ReceiveNetworkTypePresenter(
                renBtcInteractor = get(),
                userInteractor = get(),
                transactionAmountRepository = get(),
                tokenKeyProvider = get(),
                tokenInteractor = get(),
                receiveAnalytics = get(),
                environmentManager = get(),
                networkType = type
            )
        }
        factory<SendContract.Presenter> {
            SendPresenter(
                sendInteractor = get(),
                addressInteractor = get(),
                userInteractor = get(),
                searchInteractor = get(),
                burnBtcInteractor = get(),
                settingsInteractor = get(),
                tokenKeyProvider = get(),
                browseAnalytics = get(),
                analyticsInteractor = get(),
                sendAnalytics = get(),
                transactionManager = get(),
                resourcesProvider = get(),
                dispatchers = get(),
            )
        }
        factory<SearchContract.Presenter> { (usernames: List<SearchResult>) ->
            SearchPresenter(usernames = usernames, searchInteractor = get())
        }
        factory<BuySolanaContract.Presenter> { (token: Token) ->
            BuySolanaPresenter(
                tokenToBuy = token,
                moonpayRepository = get(),
                minBuyErrorFormat = get<ResourcesProvider>().getString(R.string.buy_min_error_format),
                maxBuyErrorFormat = get<ResourcesProvider>().getString(R.string.buy_max_error_format),
                buyAnalytics = get(),
                analyticsInteractor = get()
            )
        }
        factoryOf(::TokenListPresenter) bind TokenListContract.Presenter::class
        factory<ReceiveTokenContract.Presenter> { (token: Token.Active) ->
            ReceiveTokenPresenter(
                defaultToken = token,
                qrCodeInteractor = get(),
                usernameInteractor = get(),
                tokenKeyProvider = get(),
                receiveAnalytics = get()
            )
        }

        factoryOf(::ReceiveRenBtcPresenter) bind ReceiveRenBtcContract.Presenter::class

        factory<SelectTokenContract.Presenter> { (tokens: List<Token>) ->
            SelectTokenPresenter(tokens)
        }
    }
}
