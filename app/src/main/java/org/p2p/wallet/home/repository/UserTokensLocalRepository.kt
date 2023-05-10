package org.p2p.wallet.home.repository

import timber.log.Timber
import java.math.BigInteger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports
import org.p2p.wallet.home.db.TokenDao
import org.p2p.wallet.home.db.TokenEntity
import org.p2p.wallet.home.model.TokenComparator
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.Base58String

class UserTokensLocalRepository(
    private val homeLocalRepository: HomeLocalRepository,
    private val userLocalRepository: UserLocalRepository,
    private val tokenDao: TokenDao,
    private val mapper: TokenConverter
) : UserTokensRepository {
    override fun observeUserTokens(): Flow<List<Token.Active>> =
        tokenDao.getTokensFlow()
            .mapToDomain()

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
            createNewToken(tokenMint, newBalanceLamports, accountPublicKey)
                ?.also(cachedTokens::add)
                ?: kotlin.run {
                    Timber.i(NullPointerException("Failed to add new token ${tokenMint.base58Value}"))
                }
        } else {
            val tokenToChange = cachedTokens[indexOfTokenToChange]
            cachedTokens[indexOfTokenToChange] = createUpdatedToken(tokenToChange, newBalanceLamports)
        }
        homeLocalRepository.updateTokens(cachedTokens)
    }

    private fun createNewToken(
        tokenMint: Base58String,
        newBalanceLamports: BigInteger,
        accountPublicKey: Base58String
    ): Token.Active? {
        val tokenData = userLocalRepository.findTokenData(tokenMint.base58Value) ?: return null
        val price = userLocalRepository.getPriceByTokenId(tokenData.coingeckoId)
        return mapper.fromNetwork(
            mintAddress = tokenMint.base58Value,
            totalLamports = newBalanceLamports,
            accountPublicKey = accountPublicKey.base58Value,
            tokenData = tokenData,
            price = price
        )
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
