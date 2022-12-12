package org.p2p.wallet.infrastructure.network.provider

import org.p2p.core.token.Token
import org.p2p.wallet.home.repository.HomeLocalRepository

class SolanaTokenProvider(
    private val homeLocalRepository: HomeLocalRepository
) {

    suspend fun getUserSolanaToken(): Token.Active? {
        val userTokens = homeLocalRepository.getUserTokens()
        val initialToken = userTokens.find { it.isUSDC && !it.isZero }
            ?: userTokens.minBy { it.totalInLamports }

        return if (initialToken.isSOL) initialToken else userTokens.find { it.isSOL }
    }
}
