package org.p2p.wallet.newsend.interactor

import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import org.p2p.wallet.auth.username.repository.UsernameRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.newsend.model.AddressState
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance

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
        wrappedAddress: Base58String,
        sourceToken: Token.Active? = null
    ): SearchResult {
        val address = wrappedAddress.base58Value
        // assuming we are sending direct token and verify the recipient address is valid direct or SOL address
        val tokenData = transactionAddressInteractor.getDirectTokenData(address)

        if (isOwnAddress(address)) {
            return SearchResult.OwnAddressError(address, tokenData)
        }

        if (tokenData != null && isInvalidAddress(tokenData, sourceToken)) {
            return SearchResult.InvalidDirectAddress(address, tokenData)
        }

        val balance = userInteractor.getBalance(address.toBase58Instance())
        return SearchResult.AddressFound(
            addressState = AddressState(address),
            sourceToken = tokenData?.let { userInteractor.findUserToken(it.mintAddress) },
            balance = balance
        )
    }

    suspend fun isInvalidAddress(tokenData: TokenData?, sourceToken: Token.Active?): Boolean {
        val userToken = tokenData?.let { userInteractor.findUserToken(it.mintAddress) }
        val hasNoTokensToSend = tokenData != null && userToken == null
        val sendToOtherDirectToken = sourceToken != null && sourceToken.mintAddress != userToken?.mintAddress
        return hasNoTokensToSend || sendToOtherDirectToken
    }

    suspend fun isOwnAddress(publicKey: String): Boolean {
        val isOwnSolAddress = publicKey == tokenKeyProvider.publicKey
        val isOwnSplAddress = userInteractor.hasAccount(publicKey)
        return isOwnSolAddress || isOwnSplAddress
    }
}
