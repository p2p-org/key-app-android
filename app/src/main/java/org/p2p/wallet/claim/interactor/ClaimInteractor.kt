package org.p2p.wallet.claim.interactor

import org.p2p.ethereumkit.external.repository.EthereumRepository
import org.p2p.wallet.claim.api.response.FeesResponse
import org.p2p.wallet.claim.repository.EthereumClaimRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class ClaimInteractor(
    private val ethereumClaimRepository: EthereumClaimRepository,
    private val ethereumRepository: EthereumRepository,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun getClaimFees(
        tokenAddress: String?,
        amountAsString: String
    ): FeesResponse {
        val tokenAddressInHexString = ethereumRepository.getAddress().hex
        val solanaAddress = tokenKeyProvider.publicKey
        // TODO make real logic!

        return ethereumClaimRepository.getEthereumFees(
            tokenAddressInHexString,
            solanaAddress,
            tokenAddress,
            amountAsString
        )
    }
}
