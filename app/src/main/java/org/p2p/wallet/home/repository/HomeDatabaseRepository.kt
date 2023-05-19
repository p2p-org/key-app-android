package org.p2p.wallet.home.repository

import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.p2p.core.token.Token
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.scaleShort
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.home.db.TokenDao
import org.p2p.wallet.home.model.TokenConverter

private const val TAG = "HomeDatabaseRepository"

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
        if (BuildConfig.DEBUG) printUpdateTokensDiff(tokens)
        Timber.tag(TAG).d("Updating the tokens values")
        tokenDao.insertOrUpdate(tokens.map(TokenConverter::toDatabase))
    }

    private suspend fun printUpdateTokensDiff(newTokens: List<Token.Active>) {
        val oldTokens = getUserTokens()
        newTokens.forEach { newToken ->
            val oldTokenChanged = oldTokens.firstOrNull { oldToken -> oldToken.mintAddress == newToken.mintAddress }
            if (oldTokenChanged != null && oldTokenChanged != newToken) {
                Timber.tag(TAG).d(
                    buildString {
                        append("Token(${newToken.tokenSymbol}) changed: ")
                        append("${oldTokenChanged.total} -> ${newToken.total}")
                    }
                )
            } else if (oldTokenChanged == null) {
                Timber.tag(TAG).d("New token appeared: ${newToken.tokenSymbol}(${newToken.total.formatToken()})")
            }
        }
    }

    override suspend fun removeIfExists(publicKey: String, symbol: String) {
        tokenDao.removeIfExists(publicKey, symbol)
    }

    override fun getTokensFlow(): Flow<List<Token.Active>> =
        tokenDao.getTokensFlow()
            .map { it.map(TokenConverter::fromDatabase) }

    override suspend fun getUserTokens(): List<Token.Active> =
        tokenDao.getTokens()
            .map(TokenConverter::fromDatabase)

    override suspend fun setTokenHidden(mintAddress: String, visibility: String) {
        tokenDao.updateVisibility(mintAddress, visibility)
    }

    override suspend fun clear() {
        tokenDao.clearAll()
    }
}
