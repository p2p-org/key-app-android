package org.p2p.wallet.home.repository

import timber.log.Timber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.p2p.core.token.Token
import org.p2p.core.utils.formatToken
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.home.db.TokenDao
import org.p2p.wallet.home.model.TokenConverter

private const val TAG = "HomeDatabaseRepository"

class HomeDatabaseRepository(
    private val tokenDao: TokenDao
) : HomeLocalRepository {

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
