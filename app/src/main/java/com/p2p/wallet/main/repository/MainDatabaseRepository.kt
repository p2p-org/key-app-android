package com.p2p.wallet.main.repository

import com.p2p.wallet.main.db.TokenDao
import com.p2p.wallet.main.model.TokenConverter
import com.p2p.wallet.token.model.Token
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MainDatabaseRepository(
    private val tokenDao: TokenDao
) : MainLocalRepository {

    override suspend fun setTokens(tokens: List<Token>) {
        val entities = tokens.map { TokenConverter.toDatabase(it) }
        tokenDao.insertOrReplace(entities)
    }

    override fun getTokensFlow(): Flow<List<Token>> =
        tokenDao.getTokensFlow().map { entities ->
            entities.map { TokenConverter.fromDatabase(it) }
        }

    override suspend fun getTokens(): List<Token> =
        tokenDao.getTokens().map { TokenConverter.fromDatabase(it) }

    override suspend fun setTokenHidden(publicKey: String, visibility: String) {
        tokenDao.updateVisibility(publicKey, visibility)
    }

    override suspend fun clear() {
        tokenDao.clearAll()
    }
}