package org.p2p.token.service.manager

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.token.service.interactor.TokenServiceInteractor
import org.p2p.token.service.model.TokenServiceNetwork

 class TokenServiceEventPublisher(
    private val tokenServiceInteractor: TokenServiceInteractor,
    private val eventManager: TokenServiceEventManager,
    coroutineDispatcher: CoroutineDispatchers
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = coroutineDispatcher.io

    init {
        launch { observeTokenPrices(TokenServiceNetwork.SOLANA) }
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
            val eventType = TokenServiceEventType.from(networkChain)
            eventManager.notify(
                eventType = eventType,
                event = TokenServiceEvent.Loading
            )
            tokenServiceInteractor.loadMetadataForTokens(
                chain = networkChain,
                tokenAddresses = addresses
            )
        }
    }

    private suspend fun observeTokenPrices(networkChain: TokenServiceNetwork) {
        tokenServiceInteractor.getTokensPriceFlow(networkChain)
            .filterNot { it.isEmpty() }
            .collect {
                val eventType = TokenServiceEventType.from(networkChain)
                eventManager.notify(
                    eventType = eventType,
                    event = TokenServiceEvent.TokensPriceLoaded(it)
                )
                eventManager.notify(
                    eventType = eventType,
                    event = TokenServiceEvent.Idle
                )
            }
    }
}
