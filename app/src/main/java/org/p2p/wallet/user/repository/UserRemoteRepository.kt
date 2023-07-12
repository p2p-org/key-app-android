package org.p2p.wallet.user.repository

import org.p2p.core.token.TokenMetadata
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.user.api.SolanaApi

private const val ALL_TOKENS_MAP_CHUNKED_COUNT = 50

class UserRemoteRepository(
    private val solanaApi: SolanaApi
) : UserRepository {

    override suspend fun loadAllTokens(): List<TokenMetadata> =
        solanaApi.loadTokenlist()
            .tokens
            .chunked(ALL_TOKENS_MAP_CHUNKED_COUNT)
            .flatMap { chunkedList ->
                chunkedList.map { TokenConverter.fromNetwork(it) }
            }
}
