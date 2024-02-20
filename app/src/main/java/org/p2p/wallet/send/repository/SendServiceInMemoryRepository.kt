package org.p2p.wallet.send.repository

import java.math.BigInteger
import org.p2p.core.crypto.Base58String

class SendServiceInMemoryRepository {
    private var cache = mutableMapOf<Base58String, BigInteger>()

    fun putMaxAmountToSend(mintAddress: Base58String, maxAmount: BigInteger) {
        cache[mintAddress] = maxAmount
    }

    fun getMaxAmountToSend(mintAddress: Base58String): BigInteger? {
        return cache[mintAddress]
    }
}
