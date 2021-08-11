package com.p2p.wallet.user.repository

import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.api.CompareApi
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.main.model.TokenConverter
import com.p2p.wallet.main.model.TokenPrice
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.utils.scaleMedium
import com.p2p.wallet.utils.toPublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal

interface UserRepository {
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

    override suspend fun loadTokensPrices(tokens: List<String>, targetCurrency: String): List<TokenPrice> =
        withContext(Dispatchers.IO) {
            tokens
                .chunked(CHUNKED_COUNT)
                .map { list ->
                    val json = compareApi.getMultiPrice(list.joinToString(","), targetCurrency)
                    list.mapNotNull { symbol ->
                        val tokenObject = json.getAsJsonObject(symbol) ?: return@mapNotNull null
                        val price = tokenObject.getAsJsonPrimitive(Token.USD_SYMBOL).asBigDecimal
                        TokenPrice(symbol, price.scaleMedium())
                    }
                }
                .flatten()
        }

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
         * Assuming that SOL is our default token, creating it manually
         * */
        val solBalance = rpcRepository.getBalance(tokenProvider.publicKey.toPublicKey())
        val tokenData = userLocalRepository.getTokenData(Token.SOL_MINT) ?: return@withContext result
        val solPrice = userLocalRepository.getPriceByToken(tokenData.symbol)
        val token = Token.createSOL(tokenProvider.publicKey, tokenData, solBalance, solPrice.getScaledValue())
        result.add(0, token)
        return@withContext result
    }

    override suspend fun getRate(source: String, destination: String): BigDecimal {
        val json = compareApi.getPrice(source, destination)
        val price = json.getAsJsonPrimitive(destination)?.asBigDecimal ?: BigDecimal.ZERO
        return TokenPrice(source, price.scaleMedium()).price
    }
}