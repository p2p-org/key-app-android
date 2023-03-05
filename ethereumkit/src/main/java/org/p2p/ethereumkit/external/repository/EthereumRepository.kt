package org.p2p.ethereumkit.external.repository

import org.p2p.ethereumkit.external.model.EthTokenMetadata
import java.math.BigInteger

interface EthereumRepository {
    suspend fun init(seedPhrase: List<String>)
    suspend fun getBalance(): BigInteger
    suspend fun loadWalletTokens(): List<EthTokenMetadata>
}
