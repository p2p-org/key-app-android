package org.p2p.wallet.user.interactor

import timber.log.Timber
import java.math.BigInteger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token
import org.p2p.core.token.filterTokensByAvailability
import org.p2p.core.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.core.utils.fromLamports
import org.p2p.solanaj.core.PublicKey
import org.p2p.token.service.api.events.manager.TokenServiceEventPublisher
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.token.service.repository.TokenServiceRepository
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.user.repository.UserTokensLocalRepository
import org.p2p.wallet.user.repository.UserTokensRepository

class UserTokensInteractor(
    private val tokenServiceInteractor: TokenServiceEventPublisher,
    private val userTokensLocalRepository: UserTokensLocalRepository,
    private val userTokensRepository: UserTokensRepository,
    private val userLocalRepository: UserLocalRepository,
    private val tokenServiceRepository: TokenServiceRepository,
    private val dispatchers: CoroutineDispatchers
) {

    suspend fun loadAndSaveUserRates(userTokens: List<Token.Active>) {
        val tokenAddresses = userTokens.map { it.tokenServiceAddress }
        val prices = tokenServiceRepository.getTokenPricesByAddress(tokenAddresses, TokenServiceNetwork.SOLANA)
        saveUserTokensRates(prices)
    }

    suspend fun loadUserTokens(publicKey: PublicKey): List<Token.Active> {
        return userTokensRepository.loadUserTokens(publicKey)
    }

    suspend fun updateSolanaBalance(newBalanceInLamports: BigInteger) {
        val solMintAddress = WRAPPED_SOL_MINT.toBase58Instance()
        val solanaToken = userTokensLocalRepository.findTokenByMintAddress(solMintAddress) ?: return
        val newTotal = newBalanceInLamports.fromLamports(solanaToken.decimals)
        val newTotalInUsd = solanaToken.rate?.let { newTotal.times(it) }

        userTokensLocalRepository.updateTokenBalance(
            publicKey = solanaToken.publicKey.toBase58Instance(),
            newTotal = newTotal,
            newTotalInUsd = newTotalInUsd
        )
    }

    suspend fun updateOrCreateUserToken(
        programId: Base58String,
        newBalanceLamports: BigInteger,
        mintAddress: Base58String,
        publicKey: Base58String
    ) {

        val splToken = userTokensLocalRepository.findTokenByMintAddress(mintAddress)
        val tokenToUpdate = if (splToken == null) {
            // User received a new SPL token and we need to create and save it
            createNewToken(
                programId = programId,
                mintAddress = mintAddress,
                newBalanceLamports = newBalanceLamports,
                accountPublicKey = publicKey
            )
        } else {
            val newTotalAmount = newBalanceLamports.fromLamports(splToken.decimals)
            splToken.copy(
                total = newTotalAmount,
                totalInUsd = splToken.rate?.let(newTotalAmount::times)
            )
        }

        if (tokenToUpdate == null) {
            Timber.e(IllegalStateException("Token metadata for $mintAddress not found. Cannot create Token object"))
            return
        }

        userTokensLocalRepository.updateOrCreateUserToken(tokenToUpdate)
    }

    suspend fun saveUserTokens(tokens: List<Token.Active>) = withContext(dispatchers.io) {
        val cachedTokens = userTokensLocalRepository.getUserTokens()
        tokens
            .updateVisibilityState(cachedTokens)
            .let { userTokensLocalRepository.updateTokens(it) }
    }

    suspend fun saveUserTokensRates(tokensRates: List<TokenServicePrice>) = withContext(dispatchers.io) {
        userTokensLocalRepository.saveRatesForTokens(tokensRates)
    }

    suspend fun getUserTokens(): List<Token.Active> {
        return userTokensLocalRepository.getUserTokens().filterTokensByAvailability()
    }

    suspend fun hasUserToken(tokenMintAddress: String): Boolean {
        return userTokensLocalRepository.getUserTokens().any { it.mintAddress == tokenMintAddress }
    }

    fun observeUserTokens(): Flow<List<Token.Active>> {
        return userTokensLocalRepository.observeUserTokens()
            .map { it.filterTokensByAvailability() }
    }

    fun observeUserToken(mintAddress: Base58String): Flow<Token.Active> {
        return userTokensLocalRepository.observeUserToken(mintAddress)
    }

    suspend fun getUserSolToken(): Token.Active? =
        userTokensLocalRepository.getUserTokens().find { it.isSOL }

    suspend fun findUserToken(mintAddress: String): Token.Active? =
        userTokensLocalRepository.getUserTokens().find { it.mintAddress == mintAddress }

    suspend fun setTokenHidden(mintAddress: String, visibility: String) =
        userTokensLocalRepository.setTokenHidden(mintAddress, visibility)

    private suspend fun createNewToken(
        programId: Base58String,
        mintAddress: Base58String,
        newBalanceLamports: BigInteger,
        accountPublicKey: Base58String
    ): Token.Active? {

        val tokenMetadataData = userLocalRepository.findTokenData(mintAddress.base58Value) ?: return null

        val price = tokenServiceRepository.getTokenPriceByAddress(
            networkChain = TokenServiceNetwork.SOLANA,
            tokenAddress = mintAddress.base58Value,
            forceRemote = true
        )

        return TokenConverter.createToken(
            programId = programId.base58Value,
            mintAddress = mintAddress.base58Value,
            totalLamports = newBalanceLamports,
            accountPublicKey = accountPublicKey.base58Value,
            tokenMetadata = tokenMetadataData,
            price = price
        )
    }

    private fun List<Token.Active>.updateVisibilityState(cachedTokens: List<Token.Active>): List<Token.Active> =
        map { newToken ->
            // saving visibility state which user could change by this moment
            val oldToken = cachedTokens.find { oldTokens -> oldTokens.publicKey == newToken.publicKey }
            newToken.copy(visibility = oldToken?.visibility ?: newToken.visibility)
        }
}
