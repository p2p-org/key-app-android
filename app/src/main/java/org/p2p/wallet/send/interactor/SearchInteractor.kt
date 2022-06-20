package org.p2p.wallet.send.interactor

import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.send.model.AddressState
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.user.interactor.UserInteractor

private const val ZERO_BALANCE = 0L

class SearchInteractor(
    private val usernameInteractor: UsernameInteractor,
    private val userInteractor: UserInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun searchByName(username: String): List<SearchResult> {
        val usernames = usernameInteractor.resolveUsername(username)
        return usernames.map {
            val balance = userInteractor.getBalance(it.owner)
            val hasEmptyBalance = balance == ZERO_BALANCE
            return@map if (hasEmptyBalance) {
                SearchResult.EmptyBalance(AddressState(it.owner))
            } else {
                SearchResult.Full(AddressState(it.owner), it.name)
            }
        }
    }

    suspend fun searchByAddress(address: String): List<SearchResult> {
        val balance = userInteractor.getBalance(address.trim())
        val hasEmptyBalance = balance == ZERO_BALANCE
        val result = if (hasEmptyBalance) {
            SearchResult.EmptyBalance(AddressState(address))
        } else {
            SearchResult.AddressOnly(AddressState(address))
        }
        return listOf(result)
    }

    fun isOwnPublicKey(publicKey: String) = publicKey == tokenKeyProvider.publicKey
}
