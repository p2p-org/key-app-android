package org.p2p.wallet.user.local

import kotlinx.coroutines.flow.Flow
import org.p2p.wallet.receive.list.TokenListData
import org.p2p.wallet.token.model.TokenData

interface TokensQueryRepository {
    fun fetchTokens(tokens: List<TokenData>, searchText: String, refresh: Boolean)
    fun getQueryTokensFlow(): Flow<TokenListData>
}
