package org.p2p.ethereumkit.external.repository

import org.p2p.ethereumkit.external.model.EthTokenKeyProvider
import java.math.BigDecimal
import java.math.BigInteger

interface EthereumRepository {

    suspend fun getBalance(): BigInteger
}
