package org.p2p.token.service.api.events.manager

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.token.service.repository.TokenServiceRepository
import org.p2p.token.service.model.TokenServiceNetwork

class TokenServiceEventPublisher(
    private val tokenServiceInteractor: TokenServiceRepository,
    private val eventManager: TokenServiceEventManager,
    coroutineDispatcher: CoroutineDispatchers
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = coroutineDispatcher.io

    init {
        launch { observeTokenPrices(TokenServiceNetwork.SOLANA) }
        launch { observeTokenPrices(TokenServiceNetwork.ETHEREUM) }
    }

    suspend fun loadTokensPrice(networkChain: TokenServiceNetwork, addresses: List<String>) {
        eventManager.notify(
            eventType = TokenServiceEventType.from(networkChain),
            event = TokenServiceEvent.Loading
        )
        tokenServiceInteractor.loadPriceForTokens(
            chain = networkChain,
            tokenAddresses = addresses
        )
    }

    suspend fun loadTokensMetadata(networkChain: TokenServiceNetwork, addresses: List<String>) {
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

    private suspend fun observeTokenPrices(networkChain: TokenServiceNetwork) {
        tokenServiceInteractor.observeTokenPricesFlow(networkChain)
            .filterNot { it.isEmpty() }
            .distinctUntilChanged()
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
