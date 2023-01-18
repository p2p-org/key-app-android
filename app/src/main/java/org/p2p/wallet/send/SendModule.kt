package org.p2p.wallet.send

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.token.Token
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.home.ui.new.NewSelectTokenContract
import org.p2p.wallet.home.ui.new.NewSelectTokenPresenter
import org.p2p.wallet.home.ui.select.SelectTokenContract
import org.p2p.wallet.home.ui.select.SelectTokenPresenter
import org.p2p.wallet.infrastructure.network.provider.SendModeProvider
import org.p2p.wallet.newsend.repository.RecipientsDatabaseRepository
import org.p2p.wallet.newsend.repository.RecipientsLocalRepository
import org.p2p.wallet.newsend.ui.NewSendContract
import org.p2p.wallet.newsend.ui.NewSendPresenter
import org.p2p.wallet.newsend.ui.details.NewSendDetailsContract
import org.p2p.wallet.newsend.ui.details.NewSendDetailsPresenter
import org.p2p.wallet.newsend.ui.search.NewSearchContract
import org.p2p.wallet.newsend.ui.search.NewSearchPresenter
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.ui.main.SendContract
import org.p2p.wallet.send.ui.main.SendPresenter
import org.p2p.wallet.send.ui.search.SearchContract
import org.p2p.wallet.send.ui.search.SearchPresenter

object SendModule : InjectionModule {
    override fun create() = module {
        initDataLayer()
        singleOf(::SendModeProvider)
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

        factory<NewSearchContract.Presenter> { (initialToken: Token.Active?) ->
            NewSearchPresenter(
                initialToken = initialToken,
                searchInteractor = get(),
                usernameDomainFeatureToggle = get(),
                userInteractor = get(),
                newSendAnalytics = get()
            )
        }
        factoryOf(::NewSelectTokenPresenter) bind NewSelectTokenContract.Presenter::class
        factoryOf(::NewSendPresenter) bind NewSendContract.Presenter::class
        factoryOf(::NewSendDetailsPresenter) bind NewSendDetailsContract.Presenter::class
    }

    private fun Module.initDataLayer() {
        factoryOf(::RecipientsDatabaseRepository) bind RecipientsLocalRepository::class
    }
}
