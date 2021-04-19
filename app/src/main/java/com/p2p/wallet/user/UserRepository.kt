package com.p2p.wallet.user

import com.p2p.wallet.infrastructure.network.PublicKeyProvider
import com.p2p.wallet.user.model.TokenAccount
import com.p2p.wallet.user.model.UserConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.rpc.RpcClient

interface UserRepository {
    suspend fun createAccount(keys: List<String>): Account
    suspend fun loadBalance(): Long
    suspend fun loadTokens(): List<TokenAccount>
    suspend fun loadDecimals(publicKey: String): Int
}

class UserRepositoryImpl(
    private val client: RpcClient,
    private val publicKeyProvider: PublicKeyProvider
) : UserRepository {

    override suspend fun createAccount(keys: List<String>): Account = withContext(Dispatchers.IO) {
        Account.fromMnemonic(keys, "")
    }

    /**
     * Temporary passing keys here, but we should inject key provider in lower level, for example in [RpcApi]
     **/
    override suspend fun loadBalance(): Long = withContext(Dispatchers.IO) {
        client.api.getBalance(PublicKey(publicKeyProvider.publicKey))
    }

    override suspend fun loadTokens(): List<TokenAccount> = withContext(Dispatchers.IO) {
        val response = client.api.getProgramAccounts(
            PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"),
            32,
            publicKeyProvider.publicKey
        )

        return@withContext response.map { UserConverter.fromNetwork(it) }
    }

    override suspend fun loadDecimals(publicKey: String): Int = withContext(Dispatchers.IO) {
        val response = client.api.getAccountInfo(PublicKey(publicKey))
        UserConverter.fromNetwork(response.value.data ?: emptyList())
    }
}