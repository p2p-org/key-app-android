package org.p2p.wallet.home.repository

import timber.log.Timber
import java.math.BigInteger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.p2p.core.token.Token
import org.p2p.core.token.findByMintAddress
import org.p2p.core.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.core.utils.fromLamports
import org.p2p.wallet.home.db.TokenDao
import org.p2p.wallet.home.db.TokenEntity
import org.p2p.wallet.home.model.TokenComparator
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.user.repository.prices.TokenCoinGeckoId
import org.p2p.wallet.user.repository.prices.TokenPricesRemoteRepository
import org.p2p.wallet.utils.Base58String

class UserTokensLocalRepository(
    private val homeLocalRepository: HomeLocalRepository,
    private val tokenPricesRepository: TokenPricesRemoteRepository,
    private val tokenDataRepository: UserLocalRepository,
    private val tokenDao: TokenDao,
    private val mapper: TokenConverter
) : UserTokensRepository {
    override fun observeUserTokens(): Flow<List<Token.Active>> =
        tokenDao.getTokensFlow()
            .mapToDomain()

    override fun observeUserToken(tokenMint: Base58String): Flow<Token.Active> =
        observeUserTokens()
            .distinctUntilChanged()
            .map { tokens -> tokens.findByMintAddress(tokenMint.base58Value) }
            .filterNotNull()

    private fun Flow<List<TokenEntity>>.mapToDomain(): Flow<List<Token.Active>> {
        return map {
            it.map(mapper::fromDatabase)
                .sortedWith(TokenComparator())
        }
    }

    override suspend fun updateUserToken(
        newBalanceLamports: BigInteger,
        tokenMint: Base58String,
        accountPublicKey: Base58String
    ) {
        // we modify this list, it's more productive then do .map over N elements
        val cachedTokens = homeLocalRepository.getUserTokens().toMutableList()

        // faster then just map that iterates over all tokens
        val indexOfTokenToChange = cachedTokens.indexOfFirst { it.mintAddress == tokenMint.base58Value }
        val newTokenAppeared = indexOfTokenToChange == -1
        if (newTokenAppeared) {
            Timber.d("New token appeared: $tokenMint")
            createNewToken(tokenMint, newBalanceLamports, accountPublicKey)
                ?.also(cachedTokens::add)
                ?: kotlin.run {
                    Timber.e(NullPointerException("Failed to add new token ${tokenMint.base58Value}"))
                }
        } else {
            val tokenToChange = cachedTokens[indexOfTokenToChange]
            Timber.d(
                "Old token changed: %s; %s; diff=%s",
                tokenMint.base58Value, tokenToChange.tokenName, tokenToChange.totalInLamports - newBalanceLamports
            )
            cachedTokens[indexOfTokenToChange] = createUpdatedToken(tokenToChange, newBalanceLamports)
        }
        homeLocalRepository.updateTokens(cachedTokens)
    }

    private suspend fun createNewToken(
        tokenMint: Base58String,
        newBalanceLamports: BigInteger,
        accountPublicKey: Base58String
    ): Token.Active? {
        val tokenData = tokenDataRepository.findTokenData(tokenMint.base58Value) ?: return null
        val price = tokenData.coingeckoId?.let { getNewTokenPrice(it) }
        return mapper.fromNetwork(
            mintAddress = tokenMint.base58Value,
            totalLamports = newBalanceLamports,
            accountPublicKey = accountPublicKey.base58Value,
            tokenData = tokenData,
            price = price
        )
    }

    private suspend fun getNewTokenPrice(coingeckoId: String): TokenPrice? {
        return kotlin.runCatching {
            tokenPricesRepository.getTokenPriceById(
                tokenId = TokenCoinGeckoId(coingeckoId),
                targetCurrency = USD_READABLE_SYMBOL
            )
        }
            .onFailure { Timber.i(it) }
            .getOrNull()
    }

    private fun createUpdatedToken(tokenToUpdate: Token.Active, newBalance: BigInteger): Token.Active {
        val newTotalAmount = newBalance.fromLamports(tokenToUpdate.decimals)
        val newTotalInUsd = tokenToUpdate.rate?.let(newTotalAmount::times)
        return tokenToUpdate.copy(
            total = newTotalAmount,
            totalInUsd = newTotalInUsd,
        )
    }
}
