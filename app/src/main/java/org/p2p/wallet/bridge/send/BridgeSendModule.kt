package org.p2p.wallet.bridge.send

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.wallet.bridge.send.interactor.EthereumSendInteractor
import org.p2p.wallet.bridge.send.repository.EthereumSendRemoteRepository
import org.p2p.wallet.bridge.send.repository.EthereumSendRepository
import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendStateMachine
import org.p2p.wallet.bridge.send.statemachine.fee.SendBridgeFeeLoader
import org.p2p.wallet.bridge.send.statemachine.handler.bridge.AmountChangeActionHandler
import org.p2p.wallet.bridge.send.statemachine.handler.bridge.InitFeatureActionHandler
import org.p2p.wallet.bridge.send.statemachine.handler.bridge.NewTokenActionHandler
import org.p2p.wallet.bridge.send.statemachine.handler.bridge.RefreshFeeActionHandler
import org.p2p.wallet.bridge.send.statemachine.mapper.SendBridgeStaticStateMapper
import org.p2p.wallet.bridge.send.statemachine.model.SendInitialData
import org.p2p.wallet.bridge.send.statemachine.model.SendToken
import org.p2p.wallet.bridge.send.statemachine.validator.SendBridgeValidator
import org.p2p.wallet.bridge.send.ui.BridgeSendContract
import org.p2p.wallet.bridge.send.ui.BridgeSendPresenter
import org.p2p.wallet.bridge.send.ui.mapper.BridgeSendUiMapper
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.newsend.model.SearchResult

object BridgeSendModule : InjectionModule {
    override fun create() = module {
        factoryOf(::BridgeSendUiMapper)
        factoryOf(::SendFragmentFactory)
        factoryOf(::EthereumSendInteractor)
        factoryOf(::EthereumSendRemoteRepository) bind EthereumSendRepository::class
        factoryOf(::BridgeSendInteractor)
        factoryOf(::SendBridgeStaticStateMapper)
        factoryOf(::SendBridgeValidator)

        factory { (recipientAddress: SearchResult, initialToken: Token.Active?, inputAmount: BigDecimal?) ->
            val initialBridgeToken = initialToken?.let { SendToken.Bridge(it) }
            val recipient = EthAddress(recipientAddress.addressState.address)
            val initialData = SendInitialData.Bridge(
                initialToken = initialBridgeToken,
                initialAmount = inputAmount,
                recipient = recipient
            )

            val feeLoader = SendBridgeFeeLoader(
                initialData = initialData,
                mapper = get(),
                validator = get(),
                ethereumSendInteractor = get(),
                userInteractor = get(),
                sendInteractor = get(),
                feeRelayerAccountInteractor = get(),
                feeRelayerInteractor = get(),
                feeRelayerTopUpInteractor = get(),
                addressInteractor = get(),
            )
            val handlers = mutableSetOf<SendActionHandler>().apply {
                add(
                    InitFeatureActionHandler(
                        feeLoader = feeLoader,
                        initialData = initialData,
                        interactor = get()
                    )
                )
                add(
                    AmountChangeActionHandler(
                        feeLoader = feeLoader,
                        validator = get(),
                        mapper = get(),
                    )
                )
                add(
                    NewTokenActionHandler(
                        feeLoader = feeLoader,
                    )
                )
                add(
                    RefreshFeeActionHandler(
                        feeLoader = feeLoader,
                    )
                )
            }

            val stateMachine = SendStateMachine(
                handlers = handlers,
                dispatchers = get()
            )

            BridgeSendPresenter(
                recipientAddress = recipientAddress,
                userInteractor = get(),
                bridgeInteractor = get(),
                ethereumInteractor = get(),
                resources = get(),
                tokenKeyProvider = get(),
                transactionManager = get(),
                connectionStateProvider = get(),
                newSendAnalytics = get(),
                appScope = get(),
                sendModeProvider = get(),
                initialData = initialData,
                stateMachine = stateMachine,
                bridgeSendUiMapper = get()
            )
        } bind BridgeSendContract.Presenter::class
    }
}
