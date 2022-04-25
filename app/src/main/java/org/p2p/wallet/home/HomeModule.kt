package org.p2p.wallet.home

import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
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
        factory {
            val api = get<Retrofit>(named(MOONPAY_QUALIFIER)).create(MoonpayApi::class.java)
            val apiKey = BuildConfig.moonpayKey
            MoonpayRemoteRepository(api, apiKey)
        } bind MoonpayRepository::class

        factory { HomeDatabaseRepository(get()) } bind HomeLocalRepository::class

        /* Cached data exists, therefore creating singleton */
        factory {
            HomePresenter(
                appFeatureFlags = get(),
                updatesManager = get(),
                userInteractor = get(),
                settingsInteractor = get(),
                usernameInteractor = get(),
                environmentManager = get(),
                tokenKeyProvider = get(),
                homeElementItemMapper = HomeElementItemMapper()
            )
        } bind HomeContract.Presenter::class
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
        factory { SearchInteractor(get(), get(), get()) }

        factory { (token: Token.Active?) ->
            ReceiveSolanaPresenter(token, get(), get(), get(), get(), get(), get())
        } bind ReceiveSolanaContract.Presenter::class
        factory { (type: NetworkType) ->
            ReceiveNetworkTypePresenter(get(), get(), get(), get(), get(), get(), get(), type)
        } bind ReceiveNetworkTypeContract.Presenter::class
        factory { (token: Token.Active) ->
            SendPresenter(
                initialToken = token,
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
                resources = androidContext().resources
            )
        } bind SendContract.Presenter::class
        factory { (usernames: List<SearchResult>) ->
            SearchPresenter(usernames, get())
        } bind SearchContract.Presenter::class
        factory { (token: Token) ->
            BuySolanaPresenter(
                token,
                get(),
                androidContext().resources.getString(R.string.buy_min_error_format),
                androidContext().resources.getString(R.string.buy_max_error_format),
                get(), get()
            )
        } bind BuySolanaContract.Presenter::class
        factory { TokenListPresenter(get(), get(), get()) } bind TokenListContract.Presenter::class
        factory { (token: Token.Active) ->
            ReceiveTokenPresenter(
                token,
                get(),
                get(),
                get(),
                get()
            )
        } bind ReceiveTokenContract.Presenter::class

        factory {
            ReceiveRenBtcPresenter(
                get(),
                get(),
                get(),
                get(),
                get()
            )
        } bind ReceiveRenBtcContract.Presenter::class

        factory { (tokens: List<Token>) -> SelectTokenPresenter(tokens) } bind SelectTokenContract.Presenter::class
    }
}
