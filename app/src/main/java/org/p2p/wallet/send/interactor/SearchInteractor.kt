package org.p2p.wallet.send.interactor

import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import org.p2p.core.utils.Constants
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.auth.username.repository.UsernameRepository
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.send.model.AddressState
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.UsernameFormatter

class SearchInteractor(
    private val usernameRepository: UsernameRepository,
    private val usernameFormatter: UsernameFormatter,
    private val userInteractor: UserInteractor,
    private val transactionAddressInteractor: TransactionAddressInteractor,
    private val userLocalRepository: UserLocalRepository,
    private val ethereumInteractor: EthereumInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
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
                    username = usernameDetails.username.fullUsername,
                    formattedUsername = usernameFormatter.format(usernameDetails.username.fullUsername),
                )
            }
        }
    }

    suspend fun searchByAddress(
        wrappedAddress: PublicKey,
        sourceToken: Token.Active? = null
    ): SearchResult {
        val address = wrappedAddress.toBase58()
        // assuming we are sending direct token and verify the recipient address is valid direct or SOL address
        val tokenData = transactionAddressInteractor.getDirectTokenData(address)

        if (isOwnAddress(address)) {
            return SearchResult.OwnAddressError(address, tokenData)
        }

        if (tokenData != null && isInvalidAddress(tokenData, sourceToken)) {
            return SearchResult.InvalidDirectAddress(address, tokenData)
        }

        val balance = userInteractor.getBalance(wrappedAddress)
        return SearchResult.AddressFound(
            addressState = AddressState(address),
            sourceToken = tokenData?.let { userInteractor.findUserToken(it.mintAddress) },
            balance = balance
        )
    }

    suspend fun searchByEthAddress(wrappedAddress: EthAddress): SearchResult {
        val tokenData = userLocalRepository.findTokenData(Constants.WRAPPED_ETH_MINT)

        val address = wrappedAddress.hex
        if (isOwnEthAddress(address)) {
            return SearchResult.OwnAddressError(address, tokenData)
        }

        return SearchResult.AddressFound(
            addressState = AddressState(address),
            sourceToken = tokenData?.let { userInteractor.findUserToken(it.mintAddress) },
            networkType = NetworkType.ETHEREUM
        )
    }

    private suspend fun isInvalidAddress(tokenData: TokenData?, sourceToken: Token.Active?): Boolean {
        val userToken = tokenData?.let { userInteractor.findUserToken(it.mintAddress) }
        val hasNoTokensToSend = tokenData != null && userToken == null
        val sendToOtherDirectToken = sourceToken != null && sourceToken.mintAddress != userToken?.mintAddress
        return hasNoTokensToSend || sendToOtherDirectToken
    }

    private suspend fun isOwnAddress(publicKey: String): Boolean {
        val isOwnSolAddress = publicKey == tokenKeyProvider.publicKey
        val isOwnSplAddress = userInteractor.hasAccount(publicKey)
        return isOwnSolAddress || isOwnSplAddress
    }

    private suspend fun isOwnEthAddress(publicKey: String): Boolean {
        return publicKey == ethereumInteractor.getEthAddress().hex
    }
}
