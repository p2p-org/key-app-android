package com.p2p.wallet.user.repository

import com.p2p.wallet.amount.scalePrice
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.api.CompareApi
import com.p2p.wallet.main.model.TokenConverter
import com.p2p.wallet.main.model.TokenPrice
import com.p2p.wallet.rpc.RpcRepository
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.user.model.UserConverter
import com.p2p.wallet.utils.toPublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.p2p.solanaj.model.core.Account
import java.math.BigDecimal

interface UserRepository {
    suspend fun createAccount(keys: List<String>): Account
    suspend fun loadSolBalance(): Long
    suspend fun loadTokensPrices(tokens: List<String>, targetCurrency: String): List<TokenPrice>
    suspend fun loadTokens(): List<Token>
    suspend fun getRate(source: String, destination: String): BigDecimal
}

class UserRepositoryImpl(
    private val compareApi: CompareApi,
    private val tokenProvider: TokenKeyProvider,
    private val userLocalRepository: UserLocalRepository,
    private val rpcRepository: RpcRepository
) : UserRepository {

    companion object {
        private const val CHUNKED_COUNT = 30
    }

    override suspend fun createAccount(keys: List<String>): Account = withContext(Dispatchers.IO) {
        Account.fromMnemonic(keys, "")
    }

    override suspend fun loadTokensPrices(tokens: List<String>, targetCurrency: String): List<TokenPrice> =
        withContext(Dispatchers.IO) {
            tokens
                .chunked(CHUNKED_COUNT)
                .map { list ->
                    async {
                        val response = compareApi.getMultiPrice(list.joinToString(","), targetCurrency)
                        tokens.map { UserConverter.fromNetwork(it, response) }
                    }
                }
                .awaitAll()
                .flatten()
        }

    override suspend fun loadSolBalance(): Long =
        rpcRepository.getBalance(tokenProvider.publicKey.toPublicKey())

    override suspend fun loadTokens(): List<Token> = withContext(Dispatchers.IO) {
        val response = rpcRepository.getTokenAccountsByOwner(tokenProvider.publicKey.toPublicKey())

        val result = response.accounts
            .mapNotNull {
                val mintAddress = it.account.data.parsed.info.mint
                val token = userLocalRepository.getTokenData(mintAddress) ?: return@mapNotNull null
                val price = userLocalRepository.getPriceByToken(token.symbol)
                TokenConverter.fromNetwork(it, token, price)
            }
            .toMutableList()
            .also { tokens ->
                tokens.sortByDescending { it.total }
            }

        /*
         * Assuming that SOL is our default token
         * */
        val solBalance = loadSolBalance()
        val sol = Token.getSOL(tokenProvider.publicKey, solBalance)
        val solPrice = userLocalRepository.getPriceByToken(sol.tokenSymbol)
        val solExchangeRate = solPrice.getFormattedPrice()
        val element = sol.copy(
            price = sol.total.multiply(solExchangeRate),
            usdRate = solExchangeRate.scalePrice()
        )
        result.add(0, element)
        return@withContext result
    }

    override suspend fun getRate(source: String, destination: String): BigDecimal {
        val data = compareApi.getPrice(source, destination)
        return UserConverter.fromNetwork(destination, data).getFormattedPrice()
    }
}