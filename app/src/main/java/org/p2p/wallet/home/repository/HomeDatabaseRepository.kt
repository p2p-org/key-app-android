package org.p2p.wallet.home.repository

import org.p2p.wallet.home.db.TokenDao
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.model.TokenConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.p2p.wallet.home.model.TokenComparator
import org.p2p.wallet.utils.Constants.SOL_SYMBOL

class HomeDatabaseRepository(
    private val tokenDao: TokenDao
) : HomeLocalRepository {

    override suspend fun setTokens(tokens: List<Token.Active>) {
        val entities = tokens.map { TokenConverter.toDatabase(it) }
        tokenDao.insertOrReplace(entities)
    }

    override suspend fun updateTokens(tokens: List<Token.Active>) {
        val entities = tokens.map { TokenConverter.toDatabase(it) }
        tokenDao.insertOrUpdate(entities)
    }

    override suspend fun removeTemporarySol(publicKey: String) {
        tokenDao.removeIfExists(publicKey, SOL_SYMBOL)
    }

    override fun getTokensFlow(): Flow<List<Token.Active>> =
        tokenDao.getTokensFlow().map { entities ->
            entities
                .map { TokenConverter.fromDatabase(it) }
                .sortedWith(TokenComparator())
        }

    override suspend fun getUserTokens(): List<Token.Active> =
        tokenDao.getTokens().map { TokenConverter.fromDatabase(it) }

    override suspend fun setTokenHidden(mintAddress: String, visibility: String) {
        tokenDao.updateVisibility(mintAddress, visibility)
    }

    override suspend fun clear() {
        tokenDao.clearAll()
    }
}