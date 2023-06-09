package org.p2p.wallet.newsend

import android.content.res.Resources
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.token.Token
import org.p2p.wallet.R
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.feerelayer.interactor.FeeRelayerViaLinkInteractor
import org.p2p.wallet.home.ui.new.NewSelectTokenContract
import org.p2p.wallet.home.ui.new.NewSelectTokenPresenter
import org.p2p.wallet.infrastructure.network.provider.SendModeProvider
import org.p2p.wallet.infrastructure.sendvialink.UserSendLinksDatabaseRepository
import org.p2p.wallet.infrastructure.sendvialink.UserSendLinksLocalRepository
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.repository.RecipientsDatabaseRepository
import org.p2p.wallet.newsend.repository.RecipientsLocalRepository
import org.p2p.wallet.newsend.smartselection.FeeCalculator
import org.p2p.wallet.newsend.smartselection.FeeDebugInfoBuilder
import org.p2p.wallet.newsend.smartselection.SendStateManager
import org.p2p.wallet.newsend.smartselection.SmartSelectionCoordinator
import org.p2p.wallet.newsend.smartselection.handler.AmountChangedHandler
import org.p2p.wallet.newsend.smartselection.handler.FeePayerChangedHandler
import org.p2p.wallet.newsend.smartselection.handler.InitializationHandler
import org.p2p.wallet.newsend.smartselection.handler.MaxAmountEnteredHandler
import org.p2p.wallet.newsend.smartselection.handler.SourceTokenChangedHandler
import org.p2p.wallet.newsend.smartselection.handler.TriggerHandler
import org.p2p.wallet.newsend.smartselection.initial.SendInitialData
import org.p2p.wallet.newsend.smartselection.strategy.StrategyExecutor
import org.p2p.wallet.newsend.ui.details.NewSendDetailsContract
import org.p2p.wallet.newsend.ui.details.NewSendDetailsPresenter
import org.p2p.wallet.newsend.ui.main.SendButtonStateManager
import org.p2p.wallet.newsend.ui.main.SendContract
import org.p2p.wallet.newsend.ui.main.SendInputCalculator
import org.p2p.wallet.newsend.ui.main.SendPresenter
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
        initSmartSelectionDependencies()
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
                userTokensRepository = get(),
                selectedTokenMintAddress = selectedTokenMintAddress,
                selectableTokens = selectableTokens
            )
        } bind NewSelectTokenContract.Presenter::class

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

    private fun Module.initSmartSelectionDependencies() {
        factory { (initialData: SendInitialData) ->

            val sendStateManager = SendStateManager(
                initialData = initialData,
                sendInteractor = get(),
                smartSelectionCoordinator = getSmartSelectionCoordinator(initialData.recipient),
                inputCalculator = get(),
                userInteractor = get(),
                tokenKeyProvider = get(),
                transactionManager = get(),
                historyInteractor = get(),
                newSendAnalytics = get(),
                alertErrorsLogger = get(),
                appScope = get(),
                dispatchers = get()
            )

            SendPresenter(
                resources = get(),
                connectionStateProvider = get(),
                newSendAnalytics = get(),
                sendStateManager = sendStateManager,
                sendButtonStateManager = get(),
                feeDebugInfoBuilder = get()
            )
        } bind SendContract.Presenter::class


        factory { (recipient: SearchResult) ->
            val triggerHandlers: List<TriggerHandler> = get(parameters = { parametersOf(recipient) })
            SmartSelectionCoordinator(get(), triggerHandlers, get())
        }
        factoryOf(::StrategyExecutor)

        factory { (recipient: SearchResult) ->
            val feeCalculator = get<FeeCalculator>()

            listOf(
                InitializationHandler(
                    recipient = recipient,
                    feeCalculator = feeCalculator
                ),
                AmountChangedHandler(
                    recipient = recipient,
                    feeCalculator = feeCalculator
                ),
                SourceTokenChangedHandler(
                    recipient = recipient,
                    feeCalculator = feeCalculator
                ),
                MaxAmountEnteredHandler(
                    recipient = recipient,
                    feeCalculator = feeCalculator
                ),
                FeePayerChangedHandler(
                    recipient = recipient,
                    feeCalculator = feeCalculator
                )
            )
        }

        factoryOf(::InitializationHandler)
        factoryOf(::AmountChangedHandler)
        factoryOf(::SourceTokenChangedHandler)
        factoryOf(::MaxAmountEnteredHandler)
        factoryOf(::FeePayerChangedHandler)

        factoryOf(::FeeCalculator)
        factory {
            SendInputCalculator(
                sendModeProvider = get(),
                lessThenMinString = get<Resources>().getString(R.string.common_less_than_minimum)
            )
        }

        factoryOf(::SendButtonStateManager)
        factoryOf(::FeeDebugInfoBuilder)
    }

    private fun Scope.getSmartSelectionCoordinator(
        recipient: SearchResult
    ): SmartSelectionCoordinator {
        val params = { parametersOf(recipient) }
        return get(parameters = params)
    }
}
