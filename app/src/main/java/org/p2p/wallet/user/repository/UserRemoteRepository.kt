package org.p2p.wallet.user.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.model.types.Account
import org.p2p.solanaj.rpc.Environment
import org.p2p.wallet.home.api.CompareApi
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import org.p2p.wallet.rpc.repository.balance.RpcBalanceRepository
import org.p2p.wallet.user.api.SolanaApi
import org.p2p.wallet.user.model.TokenData
import org.p2p.wallet.utils.Constants.REN_BTC_DEVNET_MINT
import org.p2p.wallet.utils.Constants.REN_BTC_DEVNET_MINT_ALTERNATE
import org.p2p.wallet.utils.Constants.REN_BTC_SYMBOL
import org.p2p.wallet.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.wallet.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.wallet.utils.scaleMedium

class UserRemoteRepository(
    private val solanaApi: SolanaApi,
    private val compareApi: CompareApi,
    private val userLocalRepository: UserLocalRepository,
    private val rpcRepository: RpcAccountRepository,
    private val rpcBalanceRepository: RpcBalanceRepository,
    private val environmentManager: EnvironmentManager
) : UserRepository {

    companion object {
        private const val API_CHUNKED_COUNT = 30
        private const val ALL_TOKENS_MAP_CHUNKED_COUNT = 50
    }

    override suspend fun loadAllTokens(): List<TokenData> =
        solanaApi.loadTokenlist()
            .tokens
            .chunked(ALL_TOKENS_MAP_CHUNKED_COUNT)
            .flatMap { chunkedList ->
                chunkedList.map { TokenConverter.fromNetwork(it) }
            }

    override suspend fun loadTokensPrices(tokens: List<String>, targetCurrency: String): List<TokenPrice> =
        withContext(Dispatchers.IO) {
            val result = mutableListOf<TokenPrice>()
            tokens
                .chunked(API_CHUNKED_COUNT)
                .map { list ->
                    /**
                     * CompareApi cannot resolve more than 30 token prices at once,
                     * therefore we are splitting the tokenlist
                     * */
                    val json = compareApi.getMultiPrice(list.joinToString(","), targetCurrency)
                    val response = json.get("Response")
                    if (response?.asString == "Error") {
                        throw IllegalStateException("Cannot get rates")
                    }
                    list.forEach { symbol ->
                        val tokenObject = json.getAsJsonObject(symbol.uppercase())
                        if (tokenObject != null) {
                            val price = tokenObject.getAsJsonPrimitive(USD_READABLE_SYMBOL).asBigDecimal
                            result.add(TokenPrice(symbol, price.scaleMedium()))
                        }
                    }
                }

            return@withContext result
        }

    // TODO: 17.02.2022 save user tokens to local storage [P2PW-1315]
    override suspend fun loadTokens(publicKey: String): List<Token.Active> = withContext(Dispatchers.IO) {
        val response = rpcRepository.getTokenAccountsByOwner(publicKey)
        val result = response.accounts
            .mapNotNull {
                val mintAddress = it.account.data.parsed.info.mint

                if (mintAddress == REN_BTC_DEVNET_MINT) {
                    return@mapNotNull mapDevnetRenBTC(it)
                }

                if (mintAddress == WRAPPED_SOL_MINT) {
                    // Hiding Wrapped Sol account because we are adding native SOL lower
                    return@mapNotNull null
                }

                val token = userLocalRepository.findTokenData(mintAddress) ?: return@mapNotNull null
                val price = userLocalRepository.getPriceByToken(token.symbol)
                TokenConverter.fromNetwork(it, token, price)
            }
            .toMutableList()

        /*
         * Assuming that SOL is our default token, creating it manually
         * */
        val solBalance = rpcBalanceRepository.getBalance(publicKey)
        val tokenData = userLocalRepository.findTokenData(WRAPPED_SOL_MINT) ?: return@withContext result
        val solPrice = userLocalRepository.getPriceByToken(tokenData.symbol)
        val token = Token.createSOL(
            publicKey = publicKey,
            tokenData = tokenData,
            amount = solBalance,
            exchangeRate = solPrice?.getScaledValue()
        )
        result.add(0, token)
        return@withContext result
    }

    override suspend fun getRate(sourceSymbol: String, destinationSymbol: String): TokenPrice? {
        val json = compareApi.getPrice(sourceSymbol, destinationSymbol)
        val price = json.getAsJsonPrimitive(destinationSymbol)?.asBigDecimal
        return price?.let { TokenPrice(sourceSymbol, price.scaleMedium()) }
    }

    private fun mapDevnetRenBTC(account: Account): Token.Active? {
        if (environmentManager.loadEnvironment() != Environment.DEVNET) return null
        val token = userLocalRepository.findTokenData(REN_BTC_DEVNET_MINT)
        val result = if (token == null) {
            userLocalRepository.findTokenData(REN_BTC_DEVNET_MINT_ALTERNATE)
        } else {
            userLocalRepository.findTokenDataBySymbol(REN_BTC_SYMBOL)
        }

        if (result == null) return null

        val price = userLocalRepository.getPriceByToken(result.symbol.uppercase())
        return TokenConverter.fromNetwork(account, result, price)
    }
}
