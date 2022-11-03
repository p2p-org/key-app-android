package org.p2p.wallet.send.interactor

import org.p2p.wallet.auth.username.repository.UsernameRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.send.model.AddressState
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.Base58String

private const val ZERO_BALANCE = 0L

class SearchInteractor(
    private val usernameRepository: UsernameRepository,
    private val userInteractor: UserInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun searchByName(username: String): List<SearchResult> {
        val usernames = usernameRepository.findUsernameDetailsByUsername(username)
        return usernames.map { usernameDetails ->
            val balance = userInteractor.getBalance(usernameDetails.ownerAddress)
            val hasEmptyBalance = balance == ZERO_BALANCE
            if (hasEmptyBalance) {
                SearchResult.EmptyBalance(
                    addressState = AddressState(usernameDetails.ownerAddress.base58Value),
                )
            } else {
                SearchResult.Full(
                    addressState = AddressState(usernameDetails.ownerAddress.base58Value),
                    username = usernameDetails.username.fullUsername
                )
            }
        }
    }

    suspend fun searchByAddress(address: Base58String): List<SearchResult> {
        val balance = userInteractor.getBalance(address)
        val hasEmptyBalance = balance == ZERO_BALANCE
        val result = if (hasEmptyBalance) {
            SearchResult.EmptyBalance(AddressState(address.base58Value))
        } else {
            SearchResult.AddressOnly(AddressState(address.base58Value))
        }
        return listOf(result)
    }

    fun isOwnPublicKey(publicKey: String) = publicKey == tokenKeyProvider.publicKey
}
