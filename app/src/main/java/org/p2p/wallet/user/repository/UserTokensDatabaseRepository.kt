package org.p2p.wallet.user.repository

import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.p2p.core.crypto.Base58String
import org.p2p.core.token.Token
import org.p2p.core.token.findByMintAddress
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.scaleShort
import org.p2p.token.service.api.events.manager.TokenServiceEventPublisher
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.wallet.home.db.TokenDao
import org.p2p.wallet.home.model.TokenComparator
import org.p2p.wallet.home.model.TokenConverter

class UserTokensDatabaseRepository(
    private val userLocalRepository: UserLocalRepository,
    private val tokensDao: TokenDao,
    private val tokenConverter: TokenConverter,
    private val tokenServiceEventPublisher: TokenServiceEventPublisher
) : UserTokensLocalRepository {

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

    override fun observeUserBalance(): Flow<BigDecimal> =
        observeUserTokens()
            .map(::calculateUserBalance)
            .catch { Timber.i(it) }

    override suspend fun getUserBalance(): BigDecimal = calculateUserBalance(getUserTokens())

    private fun calculateUserBalance(tokens: List<Token.Active>): BigDecimal =
        tokens.mapNotNull(Token.Active::totalInUsd)
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .scaleShort()

    override suspend fun getUserTokens(): List<Token.Active> {
        return tokensDao.getTokens()
            .map(tokenConverter::fromDatabase)
    }

    override suspend fun clear() {
        tokensDao.clearAll()
    }

    override suspend fun saveRatesForTokens(prices: List<TokenServicePrice>) {
        val oldTokens = getUserTokens()
        val newTokens = oldTokens.map { token ->
            val newTokenRate = prices.firstOrNull { token.tokenServiceAddress == it.address }
            val oldTokenRate = token.rate

            val tokenRate = newTokenRate?.usdRate ?: oldTokenRate
            token.copy(rate = tokenRate, totalInUsd = tokenRate?.let { token.total.times(it) })
        }
        updateTokens(newTokens)
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
        return tokenConverter.createToken(
            mintAddress = tokenMint.base58Value,
            totalLamports = newBalanceLamports,
            accountPublicKey = accountPublicKey.base58Value,
            tokenMetadata = tokenData,
            price = null
        )
    }
}
