package org.p2p.token.service.converter

import java.math.BigInteger
import org.p2p.core.crypto.Base58String

interface TokenServiceAmountsConverter {
    suspend fun convertAmount(
        amountFrom: Pair<Base58String, BigInteger>,
        mintsToConvertTo: List<Base58String>
    ): Map<Base58String, BigInteger>
}
