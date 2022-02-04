package org.p2p.wallet.home

import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.moonpay.api.MoonpayApi
import org.p2p.wallet.send.interactor.SearchInteractor
import org.p2p.wallet.send.interactor.SendInteractor
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.repository.HomeDatabaseRepository
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.moonpay.repository.MoonpayRemoteRepository
import org.p2p.wallet.moonpay.repository.MoonpayRepository
import org.p2p.wallet.moonpay.ui.BuySolanaContract
import org.p2p.wallet.moonpay.ui.BuySolanaPresenter
import org.p2p.wallet.home.ui.main.HomeContract
import org.p2p.wallet.home.ui.main.MainPresenter
import org.p2p.wallet.receive.list.TokenListContract
import org.p2p.wallet.receive.list.TokenListPresenter
import org.p2p.wallet.receive.network.ReceiveNetworkTypeContract
import org.p2p.wallet.receive.network.ReceiveNetworkTypePresenter
import org.p2p.wallet.receive.solana.ReceiveSolanaContract
import org.p2p.wallet.receive.solana.ReceiveSolanaPresenter
import org.p2p.wallet.send.ui.SendContract
import org.p2p.wallet.send.ui.SendPresenter
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
        single { MainPresenter(get(), get(), get(), get(), get(), get()) } bind HomeContract.Presenter::class
        single { SendInteractor(get(), get(), get(), get(), get(), get(), get(), get()) }
        factory { SearchInteractor(get(), get()) }

        factory { (token: Token.Active?) ->
            ReceiveSolanaPresenter(token, get(), get(), get(), get())
        } bind ReceiveSolanaContract.Presenter::class
        factory { (type: NetworkType) ->
            ReceiveNetworkTypePresenter(get(), type)
        } bind ReceiveNetworkTypeContract.Presenter::class
        factory { (token: Token.Active) ->
            SendPresenter(token, get(), get(), get(), get(), get())
        } bind SendContract.Presenter::class
        factory { (usernames: List<SearchResult>) ->
            SearchPresenter(usernames, get())
        } bind SearchContract.Presenter::class
        factory { BuySolanaPresenter(get()) } bind BuySolanaContract.Presenter::class
        factory { TokenListPresenter(get()) } bind TokenListContract.Presenter::class
    }
}