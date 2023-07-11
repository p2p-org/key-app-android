package org.p2p.ethereumkit.external.repository

import java.math.BigInteger
import kotlinx.coroutines.flow.Flow
import org.p2p.core.token.Token
import org.p2p.core.wrapper.HexString
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.external.model.EthereumClaimToken
import org.p2p.ethereumkit.internal.models.Signature
import org.p2p.token.service.model.TokenServicePrice

interface EthereumRepository {
    fun init(seedPhrase: List<String>)

    suspend fun getBalance(): BigInteger
    suspend fun loadWalletTokens(claimingTokens: List<EthereumClaimToken>): List<Token.Eth>
    suspend fun cacheWalletTokens(tokens: List<Token.Eth>)
    suspend fun updateTokensRates(rates: List<TokenServicePrice>)
    fun getWalletTokensFlow(): Flow<List<Token.Eth>>

    fun getAddress(): EthAddress
    fun getPrivateKey(): BigInteger
    fun signTransaction(transaction: HexString): Signature
    fun signTransactionLegacy(transaction: HexString): Signature
}
