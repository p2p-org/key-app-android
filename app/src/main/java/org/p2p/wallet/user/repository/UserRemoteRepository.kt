package org.p2p.wallet.user.repository

import timber.log.Timber
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import org.p2p.core.utils.Constants.REN_BTC_DEVNET_MINT
import org.p2p.core.utils.Constants.REN_BTC_DEVNET_MINT_ALTERNATE
import org.p2p.core.utils.Constants.REN_BTC_SYMBOL
import org.p2p.core.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.core.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.model.types.Account
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.home.ui.main.POPULAR_TOKENS_COINGECKO_IDS
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironment
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import org.p2p.wallet.rpc.repository.balance.RpcBalanceRepository
import org.p2p.wallet.user.api.SolanaApi
import org.p2p.wallet.user.repository.prices.TokenId
import org.p2p.wallet.user.repository.prices.TokenPricesRemoteRepository

private const val ALL_TOKENS_MAP_CHUNKED_COUNT = 50

class UserRemoteRepository(
    private val solanaApi: SolanaApi,
    private val userLocalRepository: UserLocalRepository,
    private val tokenPricesRepository: TokenPricesRemoteRepository,
    private val rpcRepository: RpcAccountRepository,
    private val rpcBalanceRepository: RpcBalanceRepository,
    private val environmentManager: NetworkEnvironmentManager,
    private val dispatchers: CoroutineDispatchers
) : UserRepository {

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
    override suspend fun loadUserTokens(publicKey: PublicKey, fetchPrices: Boolean): List<Token.Active> =
        withContext(dispatchers.io) {
            val accounts = rpcRepository.getTokenAccountsByOwner(publicKey).accounts

            // Get token symbols from user accounts plus SOL
            val userTokenIds = accounts.mapNotNull {
                userLocalRepository.findTokenData(it.account.data.parsed.info.mint)?.coingeckoId
            }

            val allTokenIds = (userTokenIds + POPULAR_TOKENS_COINGECKO_IDS.map(TokenId::id)).distinct()

            // Load and save user tokens prices
            if (fetchPrices) {
                loadAndSaveUserPrices(allTokenIds)
            } else {
                checkForNewTokens(allTokenIds)
            }

            // Map accounts to List<Token.Active>
            mapAccountsToTokens(publicKey, accounts)
        }

    private suspend fun checkForNewTokens(newTokenIds: List<String>) {
        val currentTokenIds = userLocalRepository.getTokenPrices()
            .firstOrNull()
            .orEmpty()
            .map { it.tokenId }
            .toSet()
        val newTokensFound = (newTokenIds - currentTokenIds).isNotEmpty()
        if (newTokensFound) {
            loadAndSaveUserPrices(newTokenIds)
        }
    }

    private suspend fun loadAndSaveUserPrices(tokenIds: List<String>) {
        try {
            val prices = tokenPricesRepository.getTokenPriceByIds(
                tokenIds = tokenIds.map { tokenId -> TokenId(id = tokenId) },
                targetCurrency = USD_READABLE_SYMBOL
            )
            userLocalRepository.setTokenPrices(prices)
        } catch (priceError: Throwable) {
            Timber.e(priceError, "Failed to fetch initial prices")
            // do not clear the cache
        }
    }

    private suspend fun mapAccountsToTokens(publicKey: PublicKey, accounts: List<Account>): List<Token.Active> {
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
            val price = userLocalRepository.getPriceByTokenId(token.coingeckoId)
            TokenConverter.fromNetwork(it, token, price)
        }

        /*
         * Assuming that SOL is our default token, creating it manually
         * */
        val solBalance = rpcBalanceRepository.getBalance(publicKey)
        val tokenData = userLocalRepository.findTokenData(WRAPPED_SOL_MINT) ?: return tokens
        val solPrice = userLocalRepository.getPriceByTokenId(tokenData.coingeckoId)
        val solToken = Token.createSOL(
            publicKey = publicKey.toBase58(),
            tokenData = tokenData,
            amount = solBalance,
            exchangeRate = solPrice?.getScaledValue()
        )

        return listOf(solToken) + tokens
    }

    private fun mapDevnetRenBTC(account: Account): Token.Active? {
        if (environmentManager.loadCurrentEnvironment() != NetworkEnvironment.DEVNET) {
            return null
        }
        val token = userLocalRepository.findTokenData(REN_BTC_DEVNET_MINT)
        val btcTokenData: TokenData = if (token == null) {
            userLocalRepository.findTokenData(REN_BTC_DEVNET_MINT_ALTERNATE)
        } else {
            userLocalRepository.findTokenDataBySymbol(REN_BTC_SYMBOL)
        } ?: return null

        val btcPrice = userLocalRepository.getPriceByTokenId(btcTokenData.coingeckoId)
        return TokenConverter.fromNetwork(
            account = account,
            tokenData = btcTokenData,
            price = btcPrice
        )
    }
}
