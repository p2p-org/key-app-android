package org.p2p.wallet.send.interactor

import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.send.model.AddressState
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.auth.username.repository.UsernameRepository

private const val ZERO_BALANCE = 0L

class SearchInteractor(
    private val usernameRepository: UsernameRepository,
    private val userInteractor: UserInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun searchByName(username: String): List<SearchResult> {
        val usernames = usernameRepository.findUsernameDetailsByUsername(username)
        return usernames.map {
            val balance = userInteractor.getBalance(it.ownerAddress.base58Value)
            val hasEmptyBalance = balance == ZERO_BALANCE
            return@map if (hasEmptyBalance) {
                SearchResult.EmptyBalance(AddressState(it.ownerAddress.base58Value))
            } else {
                SearchResult.Full(AddressState(it.ownerAddress.base58Value), it.fullUsername)
            }
        }
    }

    suspend fun searchByAddress(address: Base58String): List<SearchResult> {
        val balance = userInteractor.getBalance(address)
        val hasEmptyBalance = balance == ZERO_BALANCE
        val result = if (hasEmptyBalance) {
            SearchResult.EmptyBalance(AddressState(address.value))
        } else {
            SearchResult.AddressOnly(AddressState(address.value))
        }
        return listOf(result)
    }

    fun isOwnPublicKey(publicKey: String) = publicKey == tokenKeyProvider.publicKey
}
