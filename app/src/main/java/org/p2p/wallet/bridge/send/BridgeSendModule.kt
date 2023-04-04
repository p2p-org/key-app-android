package org.p2p.wallet.bridge.send

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.bridge.send.repository.EthereumSendRemoteRepository
import org.p2p.wallet.bridge.send.repository.EthereumSendRepository
import org.p2p.wallet.bridge.send.ui.BridgeSendContract
import org.p2p.wallet.bridge.send.ui.BridgeSendPresenter
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.newsend.model.SearchResult

object BridgeSendModule : InjectionModule {
    override fun create() = module {
        factoryOf(::SendFragmentFactory)
        factoryOf(::EthereumSendRemoteRepository) bind EthereumSendRepository::class
        factoryOf(::BridgeSendInteractor)
        factory { (recipientAddress: SearchResult) ->
            BridgeSendPresenter(
                recipientAddress = recipientAddress,
                userInteractor = get(),
                sendInteractor = get(),
                bridgeInteractor = get(),
                resources = get(),
                tokenKeyProvider = get(),
                transactionManager = get(),
                connectionStateProvider = get(),
                newSendAnalytics = get(),
                appScope = get(),
                sendModeProvider = get(),
            )
        } bind BridgeSendContract.Presenter::class
        factoryOf(::EthereumInteractor)
    }
}
