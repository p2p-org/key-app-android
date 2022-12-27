package org.p2p.wallet.send.interactor

import org.p2p.core.token.Token
import org.p2p.wallet.auth.username.repository.UsernameRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.send.model.AddressState
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.user.interactor.UserInteractor

class SearchInteractor(
    private val usernameRepository: UsernameRepository,
    private val userInteractor: UserInteractor,
    private val transactionAddressInteractor: TransactionAddressInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun searchByName(username: String): List<SearchResult> {
        val usernames = usernameRepository.findUsernameDetailsByUsername(username)
        return usernames.map { usernameDetails ->
            val address = usernameDetails.ownerAddress.base58Value
            if (isOwnAddress(address)) {
                SearchResult.OwnAddressError(address)
            } else {
                SearchResult.UsernameFound(
                    addressState = AddressState(address = address),
                    username = usernameDetails.username.fullUsername
                )
            }
        }
    }

    suspend fun searchByAddress(
        address: String,
        sourceToken: Token.Active? = null
    ): SearchResult {
        if (isOwnAddress(address)) {
            return SearchResult.OwnAddressError(address)
        }

        // assuming we are sending direct token and verify the recipient address is valid direct or SOL address
        val tokenData = transactionAddressInteractor.getDirectTokenData(address)
        if (sourceToken != null && tokenData != null && sourceToken.mintAddress != tokenData.mintAddress) {
            return SearchResult.InvalidDirectAddress(address, tokenData)
        }

        val balance = userInteractor.getBalance(address)
        return SearchResult.AddressFound(
            addressState = AddressState(address),
            sourceToken = tokenData?.let { userInteractor.findUserToken(it.mintAddress) },
            balance = balance
        )
    }

    suspend fun isOwnAddress(publicKey: String): Boolean {
        val isOwnSolAddress = publicKey == tokenKeyProvider.publicKey
        val isOwnSplAddress = userInteractor.hasAccount(publicKey)
        return isOwnSolAddress || isOwnSplAddress
    }
}
