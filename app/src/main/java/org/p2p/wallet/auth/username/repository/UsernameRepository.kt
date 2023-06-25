package org.p2p.wallet.auth.username.repository

import org.p2p.wallet.auth.username.repository.model.UsernameDetails
import org.p2p.core.crypto.Base58String

interface UsernameRepository {
    suspend fun createUsername(username: String, ownerPublicKey: Base58String, ownerPrivateKey: Base58String)
    suspend fun findUsernameDetailsByUsername(username: String): List<UsernameDetails>
    suspend fun findUsernameDetailsByAddress(ownerAddress: Base58String): List<UsernameDetails>
    suspend fun isUsernameTaken(username: String): Boolean
}
