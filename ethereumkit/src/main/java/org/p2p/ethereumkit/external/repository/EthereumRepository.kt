package org.p2p.ethereumkit.external.repository

import java.math.BigInteger

interface EthereumRepository {

    suspend fun getBalance(): BigInteger
}
