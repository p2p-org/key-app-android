package org.p2p.wallet.user.repository

import org.p2p.solanaj.model.types.Account
import org.p2p.solanaj.rpc.Environment
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import org.p2p.wallet.rpc.repository.balance.RpcBalanceRepository
import org.p2p.wallet.user.api.SolanaApi
import org.p2p.wallet.user.model.TokenData
import org.p2p.wallet.user.repository.prices.TokenPricesRemoteRepository
import org.p2p.wallet.user.repository.prices.TokenSymbol
import org.p2p.wallet.utils.Constants.REN_BTC_DEVNET_MINT
import org.p2p.wallet.utils.Constants.REN_BTC_DEVNET_MINT_ALTERNATE
import org.p2p.wallet.utils.Constants.REN_BTC_SYMBOL
import org.p2p.wallet.utils.Constants.SOL_SYMBOL
import org.p2p.wallet.utils.Constants.WRAPPED_SOL_MINT
import kotlinx.coroutines.withContext

class UserRemoteRepository(
    private val solanaApi: SolanaApi,
    private val userLocalRepository: UserLocalRepository,
    private val tokenPricesRepository: TokenPricesRemoteRepository,
    private val rpcRepository: RpcAccountRepository,
    private val rpcBalanceRepository: RpcBalanceRepository,
    private val environmentManager: EnvironmentManager,
    private val dispatchers: CoroutineDispatchers
) : UserRepository {

    companion object {
        private const val ALL_TOKENS_MAP_CHUNKED_COUNT = 50
        private const val STANDARD_CURRENCY = "USD"
    }

    override suspend fun loadAllTokens(): List<TokenData> =
        solanaApi.loadTokenlist()
            .tokens
            .chunked(ALL_TOKENS_MAP_CHUNKED_COUNT)
            .flatMap { chunkedList ->
                chunkedList.map { TokenConverter.fromNetwork(it) }
            }

    /**
     * Load user tokens
     * @param fetchPrices if true then fetch prices as well
     */
    override suspend fun loadUserTokens(publicKey: String, fetchPrices: Boolean): List<Token.Active> =
        withContext(dispatchers.io) {
            val accounts = rpcRepository.getTokenAccountsByOwner(publicKey).accounts

            // Get token symbols from user accounts plus SOL
            val tokenSymbols = accounts.mapNotNull {
                userLocalRepository.findTokenData(it.account.data.parsed.info.mint)?.symbol
            } + SOL_SYMBOL

            // Load and save user tokens prices
            if (fetchPrices) {
                val prices = tokenPricesRepository.getTokenPricesBySymbols(
                    tokenSymbols.map { TokenSymbol(it) },
                    STANDARD_CURRENCY
                )
                userLocalRepository.setTokenPrices(prices)
            }

            // Map accounts to List<Token.Active>
            mapAccountsToTokens(publicKey, accounts)
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
        val solBalance = rpcBalanceRepository.getBalance(publicKey)
        val tokenData = userLocalRepository.findTokenData(WRAPPED_SOL_MINT) ?: return tokens
        val solPrice = userLocalRepository.getPriceByToken(tokenData.symbol)
        val solToken = Token.createSOL(
            publicKey = publicKey,
            tokenData = tokenData,
            amount = solBalance,
            exchangeRate = solPrice?.getScaledValue()
        )

        return listOf(solToken) + tokens
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
