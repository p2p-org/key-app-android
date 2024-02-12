package org.p2p.wallet.user.repository

import kotlinx.coroutines.withContext
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.model.types.Account
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.repository.TokenServiceRepository
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import org.p2p.wallet.rpc.repository.balance.RpcBalanceRepository

private const val NFT_DECIMALS = 0

class UserTokensRemoteRepository(
    private val rpcRepository: RpcAccountRepository,
    private val dispatchers: CoroutineDispatchers,
    private val tokenServiceRepository: TokenServiceRepository,
    private val userLocalRepository: UserLocalRepository,
    private val rpcBalanceRepository: RpcBalanceRepository
) : UserTokensRepository {

    override suspend fun loadUserTokens(publicKey: PublicKey): List<Token.Active> =
        withContext(dispatchers.io) {
            val accounts = rpcRepository.getTokenAccountsByOwner(publicKey).accounts
            // Map accounts to List<Token.Active>
            mapAccountsToTokens(publicKey, accounts)
        }

    private suspend fun mapAccountsToTokens(publicKey: PublicKey, accounts: List<Account>): List<Token.Active> {
        val tokens = accounts.mapNotNull { token ->
            val mintAddress = token.account.data.parsed.info.mint
            val programId = token.account.owner

            if (mintAddress == Constants.WRAPPED_SOL_MINT) {
                // Hiding Wrapped Sol account because we are adding native SOL below
                return@mapNotNull null
            }

            val tokenData = userLocalRepository.findTokenData(mintAddress) ?: return@mapNotNull null
            if (tokenData.decimals == NFT_DECIMALS) return@mapNotNull null
            val tokenPrice = tokenServiceRepository.getTokenPriceByAddress(
                tokenAddress = tokenData.mintAddress,
                networkChain = TokenServiceNetwork.SOLANA
            )
            TokenConverter.fromNetwork(
                programId = programId,
                account = token,
                tokenMetadata = tokenData,
                price = tokenPrice
            )
        }

        /*
         * Assuming that SOL is our default token, creating it manually
         * */
        val solBalance = rpcBalanceRepository.getBalance(publicKey)
        val tokenData = userLocalRepository.findTokenData(Constants.WRAPPED_SOL_MINT) ?: return tokens
        val solPrice = tokenServiceRepository.getTokenPriceByAddress(
            tokenAddress = Constants.TOKEN_SERVICE_NATIVE_SOL_TOKEN,
            networkChain = TokenServiceNetwork.SOLANA
        )
        val solToken = Token.createSOL(
            publicKey = publicKey.toBase58(),
            tokenMetadata = tokenData,
            amount = solBalance,
            solPrice = solPrice?.usdRate
        )

        return listOf(solToken) + tokens
    }
}
