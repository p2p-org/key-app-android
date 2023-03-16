package org.p2p.ethereumkit.external.repository

import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.wrapper.HexString
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.internal.models.RawTransaction
import org.p2p.ethereumkit.internal.models.Signature

interface EthereumRepository {
    fun init(seedPhrase: List<String>)
    suspend fun getBalance(): BigInteger
    suspend fun loadWalletTokens(): List<Token.Eth>
    suspend fun getAddress(): EthAddress
    fun getPrivateKey(): BigInteger
    fun signTransaction(transaction:HexString):  Pair<Signature, String>
}
