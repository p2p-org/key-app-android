package org.p2p.wallet.bridge.send

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.bridge.send.interactor.BridgeSendInteractor
import org.p2p.wallet.bridge.send.repository.EthereumSendRemoteRepository
import org.p2p.wallet.bridge.send.repository.EthereumSendRepository
import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendStateMachine
import org.p2p.wallet.bridge.send.statemachine.fee.SendBridgeTransactionLoader
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
        factoryOf(::EthereumSendRemoteRepository) bind EthereumSendRepository::class
        factoryOf(::SendBridgeStaticStateMapper)
        factoryOf(::SendBridgeValidator)
        factory {
            BridgeSendInteractor(
                ethereumSendRepository = get(),
                ethereumRepository = get(),
                userInteractor = get(),
                tokenKeyProvider = get(),
                relaySdkFacade = get(),
                dispatchers = get(),
                rpcSolanaRepository = get(),
                feeRelayerRepository = get()
            )
        }

        factory { (recipientAddress: SearchResult, initialToken: Token.Active?, inputAmount: BigDecimal?) ->
            val initialBridgeToken = initialToken?.let { SendToken.Bridge(it) }
            val recipient = EthAddress(recipientAddress.address)
            val initialData = SendInitialData.Bridge(
                initialToken = initialBridgeToken,
                initialAmount = inputAmount,
                recipient = recipient
            )

            val feeLoader = SendBridgeTransactionLoader(
                initialData = initialData,
                mapper = get(),
                validator = get(),
                bridgeSendInteractor = get(),
                feeRelayerAccountInteractor = get(),
                repository = get(),
                tokenKeyProvider = get(),
            )
            val handlers = mutableSetOf<SendActionHandler>().apply {
                add(
                    InitFeatureActionHandler(
                        transactionLoader = feeLoader,
                        initialData = initialData,
                        interactor = get()
                    )
                )
                add(
                    AmountChangeActionHandler(
                        transactionLoader = feeLoader,
                        validator = get(),
                        mapper = get(),
                    )
                )
                add(
                    NewTokenActionHandler(
                        transactionLoader = feeLoader,
                    )
                )
                add(
                    RefreshFeeActionHandler(
                        transactionLoader = feeLoader,
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
                resources = get(),
                transactionManager = get(),
                connectionStateProvider = get(),
                sendBridgesAnalytics = get(),
                appScope = get(),
                sendModeProvider = get(),
                initialData = initialData,
                stateMachine = stateMachine,
                bridgeSendUiMapper = get(),
                alarmErrorsLogger = get()
            )
        } bind BridgeSendContract.Presenter::class
        factoryOf(::EthereumInteractor)
    }
}
