package org.p2p.wallet.user.repository

import kotlinx.coroutines.withContext
import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import org.p2p.core.utils.Constants.REN_BTC_DEVNET_MINT
import org.p2p.core.utils.Constants.REN_BTC_DEVNET_MINT_ALTERNATE
import org.p2p.core.utils.Constants.REN_BTC_SYMBOL
import org.p2p.core.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.model.types.Account
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.network.environment.NetworkEnvironment
import org.p2p.core.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import org.p2p.wallet.rpc.repository.balance.RpcBalanceRepository
import org.p2p.wallet.user.api.SolanaApi

private const val ALL_TOKENS_MAP_CHUNKED_COUNT = 50

class UserRemoteRepository(
    private val solanaApi: SolanaApi,
    private val userLocalRepository: UserLocalRepository,
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
     */
    override suspend fun loadUserTokens(publicKey: PublicKey): List<Token.Active> =
        withContext(dispatchers.io) {
            val accounts = rpcRepository.getTokenAccountsByOwner(publicKey).accounts
            // Map accounts to List<Token.Active>
            mapAccountsToTokens(publicKey, accounts)
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
            solPrice = solPrice?.getScaledValue()
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
