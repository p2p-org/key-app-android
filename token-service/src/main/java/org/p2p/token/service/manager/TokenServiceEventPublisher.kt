package org.p2p.token.service.manager

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.SolAddress
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.token.service.interactor.TokenServiceInteractor
import org.p2p.token.service.model.TokenServiceNetwork

internal class TokenServiceEventPublisher(
    private val tokenServiceInteractor: TokenServiceInteractor,
    private val coroutineDispatcher: CoroutineDispatchers,
    private val eventManager: TokenServiceEventManager
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = coroutineDispatcher.io

    init {
        launch { attachToTokensPriceFlow(TokenServiceNetwork.SOLANA) }
    }

    fun loadTokensPrice(networkChain: TokenServiceNetwork, addresses: List<String>) {
        launch {
            eventManager.notify(
                eventType = TokenServiceEventType.from(networkChain),
                event = TokenServiceEvent.Loading
            )
            tokenServiceInteractor.loadPriceForTokens(
                chain = networkChain,
                tokenAddresses = addresses
            )
        }
    }

    fun loadTokensMetadata(networkChain: TokenServiceNetwork, addresses: List<String>) {
        launch {
            eventManager.notify(
                eventType = TokenServiceEventType.from(networkChain),
                event = TokenServiceEvent.Loading
            )
            tokenServiceInteractor.loadMetadataForTokens(
                chain = networkChain,
                tokenAddresses = addresses
            )
        }
    }

    private suspend fun attachToTokensPriceFlow(networkChain: TokenServiceNetwork) {
        tokenServiceInteractor.getTokensPriceFlow(networkChain).filterNot { it.isEmpty() }.collect {
            val eventType = TokenServiceEventType.from(networkChain)
            eventManager.notify(
                eventType = eventType,
                event = TokenServiceEvent.TokensPrice(it)
            )
            eventManager.notify(
                eventType = eventType,
                event = TokenServiceEvent.Idle
            )
        }
    }
}
