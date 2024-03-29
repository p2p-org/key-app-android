package org.p2p.wallet.user.repository

import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.p2p.core.crypto.Base58String
import org.p2p.core.token.Token
import org.p2p.core.utils.scaleTwo
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.wallet.home.db.TokenDao
import org.p2p.wallet.home.model.TokenComparator
import org.p2p.wallet.home.model.TokenConverter

class UserTokensDatabaseRepository(
    private val tokensDao: TokenDao
) : UserTokensLocalRepository {

    override suspend fun updateTokens(tokens: List<Token.Active>) {
        tokensDao.replaceAll(tokens.map(TokenConverter::toDatabase))
    }

    override suspend fun updateOrCreateUserToken(tokenToUpdate: Token.Active) {
        tokensDao.insertOrReplace(TokenConverter.toDatabase(tokenToUpdate))
    }

    override suspend fun removeIfExists(publicKey: String, symbol: String) {
        tokensDao.removeIfExists(publicKey, symbol)
    }

    override suspend fun findTokenByMintAddress(mintAddress: Base58String): Token.Active? {
        return tokensDao.findByMintAddress(mintAddress.base58Value)
            ?.let { TokenConverter.fromDatabase(it) }
    }

    override fun observeUserTokens(): Flow<List<Token.Active>> {
        return tokensDao.getTokensFlow()
            .map {
                it.map(TokenConverter::fromDatabase)
                    .sortedWith(TokenComparator())
            }
    }

    override fun observeUserToken(mintAddress: Base58String): Flow<Token.Active> =
        tokensDao.getSingleTokenFlow(mintAddress.base58Value)
            .map(TokenConverter::fromDatabase)

    override suspend fun updateTokenBalance(publicKey: Base58String, newTotal: BigDecimal, newTotalInUsd: BigDecimal?) {
        tokensDao.updateTokenTotal(
            publicKey = publicKey.base58Value,
            newTotal = newTotal,
            newTotalInUsd = newTotalInUsd,
        )
    }

    override suspend fun getUserBalance(): BigDecimal = calculateUserBalance(getUserTokens())

    private fun calculateUserBalance(tokens: List<Token.Active>): BigDecimal =
        tokens.mapNotNull(Token.Active::totalInUsd)
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .scaleTwo()

    override suspend fun getUserTokens(): List<Token.Active> {
        return tokensDao.getTokens()
            .map(TokenConverter::fromDatabase)
            .sortedWith(TokenComparator())
    }

    override suspend fun clear() {
        tokensDao.clearAll()
    }

    override suspend fun saveRatesForTokens(prices: List<TokenServicePrice>) {
        val oldTokens = getUserTokens()

        val newTokens = oldTokens.map { token ->
            val newTokenRate = prices.firstOrNull { token.tokenServiceAddress == it.tokenAddress }
            val oldTokenRate = token.rate
            val tokenRate = newTokenRate?.usdRate ?: oldTokenRate
            token.copy(rate = tokenRate, totalInUsd = tokenRate?.let { token.total.times(it) })
        }
        updateTokens(newTokens)
    }

    override suspend fun setTokenHidden(mintAddress: String, visibility: String) {
        tokensDao.updateVisibility(mintAddress, visibility)
    }
}
