package org.p2p.token.service.converter

import java.math.BigInteger
import org.p2p.core.crypto.Base58String
import org.p2p.core.utils.isZero
import org.p2p.token.service.api.tokenservice.TokenServiceDataSource
import org.p2p.token.service.api.tokenservice.request.TokenAmountsBodyRequest
import org.p2p.token.service.api.tokenservice.request.TokenAmountsRequest
import org.p2p.token.service.api.tokenservice.response.TokenAmountsResponse
import org.p2p.token.service.model.successOrNull

internal class TokenServiceAmountsRemoteConverter(
    private val tokenServiceDataSource: TokenServiceDataSource
) : TokenServiceAmountsConverter {
    override suspend fun convertAmount(
        amountFrom: Pair<Base58String, BigInteger>,
        mintsToConvertTo: List<Base58String>
    ): Map<Base58String, BigInteger> {

        // do not request if amount is zero, output will obviously be zero
        if (amountFrom.second.isZero()) {
            return mintsToConvertTo.associateWith { BigInteger.ZERO }
        }

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
