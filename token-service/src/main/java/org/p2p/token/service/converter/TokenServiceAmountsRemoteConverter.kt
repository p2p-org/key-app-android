package org.p2p.token.service.converter

import java.math.BigInteger
import org.p2p.core.crypto.Base58String
import org.p2p.token.service.api.TokenServiceDataSource
import org.p2p.token.service.api.request.TokenAmountsBodyRequest
import org.p2p.token.service.api.request.TokenAmountsRequest
import org.p2p.token.service.api.response.TokenAmountsResponse
import org.p2p.token.service.model.successOrNull

internal class TokenServiceAmountsRemoteConverter(
    private val tokenServiceDataSource: TokenServiceDataSource
) : TokenServiceAmountsConverter {
    override suspend fun convertAmount(
        amountFrom: Pair<Base58String, BigInteger>,
        mintsToConvertTo: List<Base58String>
    ): Map<Base58String, BigInteger> {
        val request = TokenAmountsBodyRequest(
            vsTokenMint = amountFrom.first.base58Value,
            amountLamports = amountFrom.second.toString(),
            mints = mintsToConvertTo.map(Base58String::base58Value)
        ).let { TokenAmountsRequest(it) }

        return tokenServiceDataSource.launch(request)
            .successOrNull()
            .orEmpty()
            .associateBy(
                keySelector = TokenAmountsResponse::mintAddress,
                valueTransform = TokenAmountsResponse::amountLamports
            )
    }
}
