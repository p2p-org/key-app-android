package com.p2p.wallet.user.repository

import com.p2p.wallet.utils.scaleAmount
import com.p2p.wallet.utils.scalePrice
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.api.CompareApi
import com.p2p.wallet.main.model.TokenConverter
import com.p2p.wallet.main.model.TokenPrice
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.utils.toPublicKey
import kotlinx.coroutines.Dispatchers
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
                    val json = compareApi.getMultiPrice(list.joinToString(","), targetCurrency)
                    list.mapNotNull { symbol ->
                        val tokenObject = json.getAsJsonObject(symbol) ?: return@mapNotNull null
                        val price = tokenObject.getAsJsonPrimitive(Token.USD_SYMBOL).asBigDecimal
                        TokenPrice(symbol, price.scaleAmount())
                    }
                }
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
        val json = compareApi.getPrice(source, destination)
        val price = json.getAsJsonPrimitive(destination)?.asBigDecimal ?: BigDecimal.ZERO
        return TokenPrice(source, price.scaleAmount()).price
    }
}