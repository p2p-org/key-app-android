package org.p2p.wallet.home.repository

import org.p2p.core.token.Token
import org.p2p.core.utils.scaleShort
import org.p2p.wallet.home.db.TokenDao
import org.p2p.wallet.home.model.TokenConverter
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class HomeDatabaseRepository(
    private val tokenDao: TokenDao
) : HomeLocalRepository {

    override fun observeUserBalance(): Flow<BigDecimal> =
        getTokensFlow()
            .map(::calculateUserBalance)
            .catch { Timber.i(it) }

    override suspend fun getUserBalance(): BigDecimal = calculateUserBalance(getUserTokens())

    private fun calculateUserBalance(tokens: List<Token.Active>): BigDecimal =
        tokens.mapNotNull(Token.Active::totalInUsd)
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .scaleShort()

    override suspend fun updateTokens(tokens: List<Token.Active>) {
        val entities = tokens.map { TokenConverter.toDatabase(it) }
        tokenDao.insertOrUpdate(entities)
    }

    override suspend fun removeIfExists(publicKey: String, symbol: String) {
        tokenDao.removeIfExists(publicKey, symbol)
    }

    override fun getTokensFlow(): Flow<List<Token.Active>> =
        tokenDao.getTokensFlow()
            .map { it.map(TokenConverter::fromDatabase) }

    override suspend fun getUserTokens(): List<Token.Active> =
        tokenDao.getTokens().map { TokenConverter.fromDatabase(it) }

    override suspend fun setTokenHidden(mintAddress: String, visibility: String) {
        tokenDao.updateVisibility(mintAddress, visibility)
    }

    override suspend fun clear() {
        tokenDao.clearAll()
    }
}
