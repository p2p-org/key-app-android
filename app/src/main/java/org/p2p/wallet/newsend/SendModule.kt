package org.p2p.wallet.newsend

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.interactor.FeeRelayerViaLinkInteractor
import org.p2p.wallet.home.ui.new.NewSelectTokenContract
import org.p2p.wallet.home.ui.new.NewSelectTokenPresenter
import org.p2p.wallet.infrastructure.network.provider.SendModeProvider
import org.p2p.wallet.infrastructure.sendvialink.UserSendLinksDatabaseRepository
import org.p2p.wallet.infrastructure.sendvialink.UserSendLinksLocalRepository
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.repository.RecipientsDatabaseRepository
import org.p2p.wallet.newsend.repository.RecipientsLocalRepository
import org.p2p.wallet.newsend.ui.NewSendContract
import org.p2p.wallet.newsend.ui.NewSendPresenter
import org.p2p.wallet.newsend.ui.SendOpenedFrom
import org.p2p.wallet.newsend.ui.details.NewSendDetailsContract
import org.p2p.wallet.newsend.ui.details.NewSendDetailsPresenter
import org.p2p.wallet.newsend.ui.search.NewSearchContract
import org.p2p.wallet.newsend.ui.search.NewSearchPresenter
import org.p2p.wallet.svl.interactor.ReceiveViaLinkInteractor
import org.p2p.wallet.svl.interactor.SendViaLinkInteractor
import org.p2p.wallet.svl.model.ReceiveViaLinkMapper
import org.p2p.wallet.svl.ui.linkgeneration.SendLinkGenerationContract
import org.p2p.wallet.svl.ui.linkgeneration.SendLinkGenerationPresenter
import org.p2p.wallet.svl.ui.receive.ReceiveViaLinkContract
import org.p2p.wallet.svl.ui.receive.ReceiveViaLinkPresenter
import org.p2p.wallet.svl.ui.send.SendViaLinkContract
import org.p2p.wallet.svl.ui.send.SendViaLinkPresenter

object SendModule : InjectionModule {
    override fun create() = module {
        initDataLayer()
        singleOf(::SendModeProvider)

        factory<NewSearchContract.Presenter> { (initialToken: Token.Active?) ->
            NewSearchPresenter(
                initialToken = initialToken,
                searchInteractor = get(),
                usernameDomainFeatureToggle = get(),
                userInteractor = get(),
                newSendAnalytics = get(),
                sendViaLinkFeatureToggle = get(),
                feeRelayerAccountInteractor = get(),
                ethAddressEnabledFeatureToggle = get()
            )
        }
        factory { (selectedTokenMintAddress: String?, selectableTokens: List<Token.Active>?) ->
            NewSelectTokenPresenter(
                tokenServiceCoordinator = get(),
                selectedTokenMintAddress = selectedTokenMintAddress,
                selectableTokens = selectableTokens
            )
        } bind NewSelectTokenContract.Presenter::class
        factory { (recipient: SearchResult, openedFrom: SendOpenedFrom) ->
            NewSendPresenter(
                recipientAddress = recipient,
                openedFrom = openedFrom,
                userInteractor = get(),
                sendInteractor = get(),
                resources = get(),
                tokenKeyProvider = get(),
                transactionManager = get(),
                connectionStateProvider = get(),
                newSendAnalytics = get(),
                appScope = get(),
                sendModeProvider = get(),
                alertErrorsLogger = get(),
                historyInteractor = get(),
                userTokensInteractor = get(),
                tokenServiceCoordinator = get()
            )
        } bind NewSendContract.Presenter::class
        factoryOf(::NewSendDetailsPresenter) bind NewSendDetailsContract.Presenter::class

        factoryOf(::SendLinkGenerationPresenter) bind SendLinkGenerationContract.Presenter::class
        factoryOf(::SendViaLinkPresenter) bind SendViaLinkContract.Presenter::class
        factoryOf(::ReceiveViaLinkPresenter) bind ReceiveViaLinkContract.Presenter::class
        factoryOf(::ReceiveViaLinkInteractor)
        factoryOf(::ReceiveViaLinkMapper)
    }

    private fun Module.initDataLayer() {
        factoryOf(::RecipientsDatabaseRepository) bind RecipientsLocalRepository::class
        factoryOf(::FeeRelayerViaLinkInteractor)
        factoryOf(::SendViaLinkInteractor)
        factoryOf(::UserSendLinksDatabaseRepository) bind UserSendLinksLocalRepository::class
    }
}
