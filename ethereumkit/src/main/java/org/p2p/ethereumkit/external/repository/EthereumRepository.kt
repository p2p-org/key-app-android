package org.p2p.ethereumkit.external.repository

import java.math.BigInteger
import org.p2p.ethereumkit.external.model.EthTokenMetadata

interface EthereumRepository {
    fun init(seedPhrase: List<String>)
    suspend fun getBalance(): BigInteger
    suspend fun loadWalletTokens(): List<EthTokenMetadata>
}
