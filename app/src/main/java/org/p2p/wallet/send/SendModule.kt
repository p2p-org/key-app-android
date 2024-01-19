package org.p2p.wallet.send

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.network.NetworkCoreModule.getRetrofit
import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.interactor.FeeRelayerViaLinkInteractor
import org.p2p.wallet.home.ui.new.NewSelectTokenContract
import org.p2p.wallet.home.ui.new.NewSelectTokenPresenter
import org.p2p.wallet.infrastructure.network.provider.SendModeProvider
import org.p2p.wallet.infrastructure.sendvialink.UserSendLinksDatabaseRepository
import org.p2p.wallet.infrastructure.sendvialink.UserSendLinksLocalRepository
import org.p2p.wallet.send.api.SendServiceApi
import org.p2p.wallet.send.interactor.usecase.CalculateSendServiceFeesUseCase
import org.p2p.wallet.send.interactor.usecase.CalculateToken2022TransferFeeUseCase
import org.p2p.wallet.send.interactor.usecase.GetFeesInPayingTokenUseCase
import org.p2p.wallet.send.interactor.usecase.GetTokenExtensionsUseCase
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.repository.RecipientsDatabaseRepository
import org.p2p.wallet.send.repository.RecipientsLocalRepository
import org.p2p.wallet.send.repository.SendServiceRemoteRepository
import org.p2p.wallet.send.repository.SendServiceRepository
import org.p2p.wallet.send.ui.NewSendContract
import org.p2p.wallet.send.ui.NewSendPresenter
import org.p2p.wallet.send.ui.SendOpenedFrom
import org.p2p.wallet.send.ui.details.NewSendDetailsContract
import org.p2p.wallet.send.ui.details.NewSendDetailsPresenter
import org.p2p.wallet.send.ui.search.NewSearchContract
import org.p2p.wallet.send.ui.search.NewSearchPresenter
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

    private const val SEND_SERVICE_RETROFIT_QUALIFIER = "SEND_SERVICE_RETROFIT_QUALIFIER"

    override fun create() = module {
        initDataLayer()
        initSendService()
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
        factoryOf(::SendFeeRelayerManager)

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
                tokenServiceCoordinator = get(),
                sendFeeRelayerManager = get(),
            )
        } bind NewSendContract.Presenter::class
        factoryOf(::NewSendDetailsPresenter) bind NewSendDetailsContract.Presenter::class

        factoryOf(::SendLinkGenerationPresenter) bind SendLinkGenerationContract.Presenter::class
        factoryOf(::SendViaLinkPresenter) bind SendViaLinkContract.Presenter::class
        factoryOf(::ReceiveViaLinkPresenter) bind ReceiveViaLinkContract.Presenter::class
        factoryOf(::ReceiveViaLinkInteractor)
        factoryOf(::ReceiveViaLinkMapper)

        // todo: this is a last resort solution, because logic is too complex and needs to be refactored
        //       these use cases extracted to avoid circular dependencies between SendInteractor and SendFeeRelayerManager
        factoryOf(::GetFeesInPayingTokenUseCase)
        factoryOf(::CalculateToken2022TransferFeeUseCase)
        factoryOf(::GetTokenExtensionsUseCase)
        factoryOf(::CalculateSendServiceFeesUseCase)
    }

    private fun Module.initDataLayer() {
        factoryOf(::RecipientsDatabaseRepository) bind RecipientsLocalRepository::class
        factoryOf(::FeeRelayerViaLinkInteractor)
        factoryOf(::SendViaLinkInteractor)
        factoryOf(::UserSendLinksDatabaseRepository) bind UserSendLinksLocalRepository::class
    }

    private fun Module.initSendService() {
        single(named(SEND_SERVICE_RETROFIT_QUALIFIER)) {
            // todo: move to somewhere
            val rpcApiUrl = "https://send-service.key.app/"
            getRetrofit(
                baseUrl = rpcApiUrl,
                tag = "SendServiceRpc",
                interceptor = null
//                interceptor = RpcInterceptor(get(), get())
            )
        }

        factory {
            val api = get<Retrofit>(named(SEND_SERVICE_RETROFIT_QUALIFIER)).create(SendServiceApi::class.java)
            SendServiceRemoteRepository(api)
        } bind SendServiceRepository::class
    }
}
