package org.p2p.wallet.main.repository

import org.p2p.wallet.main.db.TokenDao
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.model.TokenConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MainDatabaseRepository(
    private val tokenDao: TokenDao
) : MainLocalRepository {

    override suspend fun setTokens(tokens: List<Token.Active>) {
        val entities = tokens.map { TokenConverter.toDatabase(it) }
        tokenDao.insertOrReplace(entities)
    }

    override suspend fun updateTokens(tokens: List<Token.Active>) {
        val entities = tokens.map { TokenConverter.toDatabase(it) }
        tokenDao.insertOrUpdate(entities)
    }

    override fun getTokensFlow(): Flow<List<Token.Active>> =
        tokenDao.getTokensFlow().map { entities ->
            entities
                .map { TokenConverter.fromDatabase(it) }
                .sortedByDescending { it.totalInUsd }
                .sortedByDescending { it.isSOL }
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