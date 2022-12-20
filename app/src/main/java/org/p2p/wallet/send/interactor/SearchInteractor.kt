package org.p2p.wallet.send.interactor

import org.p2p.core.token.Token
import org.p2p.wallet.R
import org.p2p.wallet.auth.username.repository.UsernameRepository
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.feerelayer.model.RelayInfo
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.send.model.AddressState
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.Base58String

class SearchInteractor(
    private val usernameRepository: UsernameRepository,
    private val userInteractor: UserInteractor,
    private val transactionAddressInteractor: TransactionAddressInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val resourcesProvider: ResourcesProvider
) {

    suspend fun searchByName(username: String): List<SearchResult> {
        val usernames = usernameRepository.findUsernameDetailsByUsername(username)
        return usernames.map { usernameDetails ->
            val address = usernameDetails.ownerAddress.base58Value
            if (isOwnPublicKey(address)) {
                SearchResult.InvalidResult(
                    AddressState(address = address),
                    errorMessage = resourcesProvider.getString(
                        R.string.search_yourself_error
                    ),
                    description = resourcesProvider.getString(
                        R.string.search_yourself_description
                    ),
                )
            } else {
                SearchResult.UsernameFound(
                    addressState = AddressState(address = address),
                    username = usernameDetails.username.fullUsername
                )
            }
        }
    }

    suspend fun searchByAddress(address: Base58String, sourceToken: Token.Active? = null): List<SearchResult> {
        val tokenData = transactionAddressInteractor.getTokenDataIfDirect(address)
        val userToken = tokenData?.let { userInteractor.findUserToken(it.mintAddress) }
        val hasNoTokensToSend = tokenData != null && userToken == null
        val relayInfo = feeRelayerAccountInteractor.getRelayInfo()
        val userTokens = userInteractor.getUserTokens()
        val hasNotEnoughFunds = !userTokens.any { it.hasFundsForSend(relayInfo) }
        val isOwnAddress = userToken?.publicKey == address.base58Value
        val addressState = AddressState(address.base58Value)
        val sendToOtherDirectToken = sourceToken != null && sourceToken.mintAddress != userToken?.mintAddress
        return listOf(
            when {
                isOwnAddress -> SearchResult.InvalidResult(
                    addressState = addressState,
                    errorMessage = resourcesProvider.getString(
                        R.string.search_yourself_error
                    ),
                    tokenData = tokenData,
                    description = resourcesProvider.getString(
                        R.string.search_yourself_description
                    ),
                )
                hasNoTokensToSend || sendToOtherDirectToken -> SearchResult.InvalidResult(
                    addressState = addressState,
                    errorMessage = resourcesProvider.getString(
                        R.string.search_no_other_tokens_error
                    ),
                    tokenData = tokenData,
                    description = resourcesProvider.getString(
                        R.string.search_no_other_tokens_description,
                        tokenData?.symbol.orEmpty()
                    ),
                )
                hasNotEnoughFunds -> SearchResult.InvalidResult(
                    addressState = addressState,
                    errorMessage = resourcesProvider.getString(
                        R.string.search_no_founds_error,
                        tokenData?.symbol.orEmpty()
                    ),
                    canReceiveAndBuy = true
                )
                else -> SearchResult.AddressOnly(addressState, userToken)
            }
        )
    }

    fun isOwnPublicKey(publicKey: String) = publicKey == tokenKeyProvider.publicKey

    private fun Token.Active.hasFundsForSend(relayInfo: RelayInfo): Boolean {
        // TODO modify logic according to business needs
        return totalInLamports > relayInfo.minimumTokenAccountRent +
            (relayInfo.minimumRelayAccountRent * 2.toBigInteger())
    }
}
