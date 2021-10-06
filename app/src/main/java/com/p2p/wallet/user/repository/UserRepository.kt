package com.p2p.wallet.user.repository

import com.p2p.wallet.infrastructure.network.environment.EnvironmentManager
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
import org.p2p.solanaj.model.types.Account
import org.p2p.solanaj.rpc.Environment
import java.math.BigDecimal

interface UserRepository {
    suspend fun loadTokensPrices(tokens: List<String>, targetCurrency: String): List<TokenPrice>
    suspend fun loadTokens(): List<Token>
    suspend fun getRate(sourceSymbol: String, destinationSymbol: String): BigDecimal
}

class UserRepositoryImpl(
    private val compareApi: CompareApi,
    private val tokenProvider: TokenKeyProvider,
    private val userLocalRepository: UserLocalRepository,
    private val rpcRepository: RpcRepository,
    private val environmentManager: EnvironmentManager
) : UserRepository {

    companion object {
        private const val CHUNKED_COUNT = 30
    }

    override suspend fun loadTokensPrices(tokens: List<String>, targetCurrency: String): List<TokenPrice> =
        withContext(Dispatchers.IO) {
            val result = mutableListOf<TokenPrice>()
            tokens
                .chunked(CHUNKED_COUNT)
                .map { list ->
                    val json = compareApi.getMultiPrice(list.joinToString(","), targetCurrency)
                    list.forEach { symbol ->
                        val tokenObject = json.getAsJsonObject(symbol.uppercase())
                        if (tokenObject != null) {
                            val price = tokenObject.getAsJsonPrimitive(Token.USD_SYMBOL).asBigDecimal
                            result.add(TokenPrice(symbol, price.scaleMedium()))
                        }
                    }
                }

            return@withContext result
        }

    override suspend fun loadTokens(): List<Token> = withContext(Dispatchers.IO) {
        val response = rpcRepository.getTokenAccountsByOwner(tokenProvider.publicKey.toPublicKey())

        val result = response.accounts
            .mapNotNull {
                val mintAddress = it.account.data.parsed.info.mint

                if (mintAddress == Token.REN_BTC_DEVNET_MINT) {
                    return@mapNotNull addDevnetRenBTC(it)
                }

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
        val solBalance = rpcRepository.getBalance(tokenProvider.publicKey)
        val tokenData = userLocalRepository.getTokenData(Token.WRAPPED_SOL_MINT) ?: return@withContext result
        val solPrice = userLocalRepository.getPriceByToken(tokenData.symbol)
        val token = Token.createSOL(tokenProvider.publicKey, tokenData, solBalance, solPrice.getScaledValue())
        result.add(0, token)
        return@withContext result
    }

    override suspend fun getRate(sourceSymbol: String, destinationSymbol: String): BigDecimal {
        val json = compareApi.getPrice(sourceSymbol, destinationSymbol)
        val price = json.getAsJsonPrimitive(destinationSymbol)?.asBigDecimal ?: BigDecimal.ZERO
        return TokenPrice(sourceSymbol, price.scaleMedium()).price
    }

    private fun addDevnetRenBTC(account: Account): Token? {
        if (environmentManager.loadEnvironment() != Environment.DEVNET) return null
        val token = userLocalRepository.getTokenData(Token.REN_BTC_DEVNET_MINT) ?: return null
        val price = userLocalRepository.getPriceByToken(token.symbol.uppercase())
        return TokenConverter.fromNetwork(account, token, price)
    }
}