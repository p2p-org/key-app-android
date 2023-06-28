package org.p2p.wallet.user.repository

import java.math.BigInteger
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.p2p.core.crypto.Base58String
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token
import org.p2p.core.token.findByMintAddress
import org.p2p.core.utils.fromLamports
import org.p2p.token.service.manager.TokenServiceEvent
import org.p2p.token.service.manager.TokenServiceEventListener
import org.p2p.token.service.manager.TokenServiceEventManager
import org.p2p.token.service.manager.TokenServiceEventPublisher
import org.p2p.token.service.manager.TokenServiceEventType
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.wallet.home.db.TokenDao
import org.p2p.wallet.home.model.TokenComparator
import org.p2p.wallet.home.model.TokenConverter

class UserTokensDatabaseRepository(
    private val tokenServiceEventManager: TokenServiceEventManager,
    private val userLocalRepository: UserLocalRepository,
    private val tokensDao: TokenDao,
    private val tokenConverter: TokenConverter,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val tokenServiceEventPublisher: TokenServiceEventPublisher
) : UserTokensLocalRepository, CoroutineScope {
    override val coroutineContext: CoroutineContext = coroutineDispatchers.io

    init {
        tokenServiceEventManager.subscribe(TokenServiceEventSubscriber(::updatePricesForTokens))
    }

    override suspend fun updateTokens(tokens: List<Token.Active>) {
        tokensDao.insertOrUpdate(tokens.map(tokenConverter::toDatabase))
    }

    override suspend fun updateUserToken(
        newBalanceLamports: BigInteger,
        mintAddress: Base58String,
        publicKey: Base58String
    ) {
        val tokenToUpdate = findTokenByMintAddress(mintAddress)
            ?.let { createUpdatedToken(it, newBalanceLamports) }
            ?: createNewToken(mintAddress, newBalanceLamports, publicKey)

        if (tokenToUpdate != null) {
            tokensDao.insertOrUpdate(
                entities = listOf(tokenConverter.toDatabase(tokenToUpdate))
            )
        }
    }

    override suspend fun removeIfExists(publicKey: String, symbol: String) {
        tokensDao.removeIfExists(publicKey, symbol)
    }

    override suspend fun findTokenByMintAddress(mintAddress: Base58String): Token.Active? {
        return tokensDao.findByMintAddress(mintAddress.base58Value)
            ?.let(tokenConverter::fromDatabase)
    }

    override fun observeUserTokens(): Flow<List<Token.Active>> {
        return tokensDao.getTokensFlow()
            .map { tokenEntities ->
                tokenEntities.map(tokenConverter::fromDatabase)
                    .sortedWith(TokenComparator())
            }
    }

    override fun observeUserToken(mintAddress: Base58String): Flow<Token.Active> {
        return observeUserTokens().distinctUntilChanged()
            .map { tokens -> tokens.findByMintAddress(mintAddress.base58Value) }
            .filterNotNull()
    }

    override suspend fun getUserTokens(): List<Token.Active> {
        return tokensDao.getTokens()
            .map(tokenConverter::fromDatabase)
    }

    override suspend fun clear() {
        tokensDao.clearAll()
    }

    private fun updatePricesForTokens(prices: Map<String, TokenServicePrice>) {
        launch {
            val oldTokens = getUserTokens()
            val newTokens = oldTokens.map { token ->
                val tokenRate = prices[token.mintAddress]?.price
                token.copy(rate = tokenRate)
            }
            updateTokens(newTokens)
        }
    }

    private fun createUpdatedToken(tokenToUpdate: Token.Active, newBalance: BigInteger): Token.Active {
        val newTotalAmount = newBalance.fromLamports(tokenToUpdate.decimals)
        val newTotalInUsd = tokenToUpdate.rate?.let(newTotalAmount::times)
        return tokenToUpdate.copy(
            total = newTotalAmount,
            totalInUsd = newTotalInUsd
        )
    }

    private suspend fun createNewToken(
        tokenMint: Base58String,
        newBalanceLamports: BigInteger,
        accountPublicKey: Base58String
    ): Token.Active? {

        val tokenData = userLocalRepository.findTokenData(tokenMint.base58Value) ?: return null
        tokenServiceEventPublisher.loadTokensPrice(
            networkChain = TokenServiceNetwork.SOLANA,
            addresses = listOf(tokenMint.base58Value)
        )
        return tokenConverter.fromNetwork(
            mintAddress = tokenMint.base58Value,
            totalLamports = newBalanceLamports,
            accountPublicKey = accountPublicKey.base58Value,
            tokenData = tokenData,
            price = null
        )
    }

    inner class TokenServiceEventSubscriber(private val block: (Map<String, TokenServicePrice>) -> Unit) :
        TokenServiceEventListener {

        override fun onUpdate(eventType: TokenServiceEventType, event: TokenServiceEvent) {
            if (eventType != TokenServiceEventType.SOLANA_CHAIN_EVENT) return
            if (event !is TokenServiceEvent.TokensPriceLoaded) return
            val prices = event.result
            block(prices)
        }
    }
}
