package org.p2p.wallet.send.repository

import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token

/**
 * Check if a token can be used as fee payer using send-service compensation tokens
 */
class FeePayerTokenValidityRepository(
    private val dispatchers: CoroutineDispatchers,
    private val sendServiceRepository: SendServiceRepository,
) {

    private val allowedTokenMintsCache = mutableSetOf<Base58String>()

    suspend fun checkIsValid(token: Token.Active): Boolean =
        checkIsValid(token.mintAddressB58)

    private suspend fun checkIsValid(tokenMint: Base58String): Boolean = withContext(dispatchers.io) {
        if (allowedTokenMintsCache.isEmpty()) {
            allowedTokenMintsCache += sendServiceRepository.getCompensationTokens()
        }

        tokenMint in allowedTokenMintsCache
    }
}
