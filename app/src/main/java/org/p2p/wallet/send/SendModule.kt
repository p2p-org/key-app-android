package org.p2p.wallet.send

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.ui.new.NewSelectTokenContract
import org.p2p.wallet.home.ui.new.NewSelectTokenPresenter
import org.p2p.wallet.home.ui.select.SelectTokenContract
import org.p2p.wallet.home.ui.select.SelectTokenPresenter
import org.p2p.wallet.newsend.NewSendContract
import org.p2p.wallet.newsend.NewSendPresenter
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.ui.main.SendContract
import org.p2p.wallet.send.ui.main.SendPresenter
import org.p2p.wallet.send.ui.search.NewSearchContract
import org.p2p.wallet.send.ui.search.NewSearchPresenter
import org.p2p.wallet.send.ui.search.SearchContract
import org.p2p.wallet.send.ui.search.SearchPresenter

object SendModule : InjectionModule {
    override fun create() = module {
        factory<SelectTokenContract.Presenter> { (tokens: List<Token>) ->
            SelectTokenPresenter(tokens)
        }
        factory<SearchContract.Presenter> { (usernames: List<SearchResult>) ->
            SearchPresenter(usernames = usernames, searchInteractor = get(), usernameDomainFeatureToggle = get())
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
                usernameDomainFeatureToggle = get(),
                dispatchers = get()
            )
        }

        factory<NewSearchContract.Presenter> { (usernames: List<SearchResult>) ->
            NewSearchPresenter(
                usernames = usernames,
                searchInteractor = get(),
                usernameDomainFeatureToggle = get()
            )
        }
        factoryOf(::NewSelectTokenPresenter) bind NewSelectTokenContract.Presenter::class
        factoryOf(::NewSendPresenter) bind NewSendContract.Presenter::class
    }
}
