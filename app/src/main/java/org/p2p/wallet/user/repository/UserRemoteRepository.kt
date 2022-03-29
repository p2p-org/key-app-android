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
import org.p2p.wallet.utils.Constants.SOL_SYMBOL
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
        private const val BALANCE_CURRENCY = "USD"
    }

    override suspend fun loadAllTokens(): List<TokenData> =
        solanaApi.loadTokenlist()
            .tokens
            .chunked(ALL_TOKENS_MAP_CHUNKED_COUNT)
            .flatMap { chunkedList ->
                chunkedList.map { TokenConverter.fromNetwork(it) }
            }

    /**
     * Load user tokens and their prices
     */
    override suspend fun loadUserTokens(publicKey: String): List<Token.Active> = withContext(Dispatchers.IO) {
        val accounts = rpcRepository.getTokenAccountsByOwner(publicKey).accounts

        // Get token symbols from user accounts plus SOL
        val tokenSymbols = accounts.mapNotNull {
            userLocalRepository.findTokenData(it.account.data.parsed.info.mint)?.symbol
        } + SOL_SYMBOL

        // Load and save user tokens prices
        val prices = loadTokensPrices(tokenSymbols.toSet(), BALANCE_CURRENCY)
        userLocalRepository.setTokenPrices(prices)

        // Map accounts to List<Token.Active>
        mapAccountsToTokens(publicKey, accounts)
    }

    private suspend fun loadTokensPrices(tokens: Set<String>, targetCurrency: String): List<TokenPrice> {
        val prices = mutableListOf<TokenPrice>()

        tokens.chunked(API_CHUNKED_COUNT)
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
                        prices.add(TokenPrice(symbol, price.scaleMedium()))
                    }
                }
            }

        return prices
    }

    private suspend fun mapAccountsToTokens(publicKey: String, accounts: List<Account>): List<Token.Active> {
        val tokens = accounts.mapNotNull {
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

        /*
         * Assuming that SOL is our default token, creating it manually
         * */
        return addSolToken(publicKey, tokens)
    }

    private suspend fun addSolToken(publicKey: String, tokens: List<Token.Active>): List<Token.Active> {
        val solBalance = rpcBalanceRepository.getBalance(publicKey)
        val tokenData = userLocalRepository.findTokenData(WRAPPED_SOL_MINT) ?: return tokens
        val solPrice = userLocalRepository.getPriceByToken(tokenData.symbol)
        val solToken = Token.createSOL(
            publicKey = publicKey,
            tokenData = tokenData,
            amount = solBalance,
            exchangeRate = solPrice?.getScaledValue()
        )

        return tokens + solToken
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
