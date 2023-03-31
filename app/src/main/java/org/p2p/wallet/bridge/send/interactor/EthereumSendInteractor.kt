package org.p2p.wallet.bridge.send.interactor

import org.p2p.core.token.SolAddress
import org.p2p.ethereumkit.external.repository.EthereumRepository
import org.p2p.wallet.bridge.send.model.BridgeSendFees
import org.p2p.wallet.bridge.send.repository.EthereumSendRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class EthereumSendInteractor(
    private val ethereumSendRepository: EthereumSendRepository,
    private val ethereumRepository: EthereumRepository,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun getSendFee(
        sendTokenMint: SolAddress?,
        amount: String
    ): BridgeSendFees {
        val userAddress = SolAddress(tokenKeyProvider.publicKey)
        val ethereumAddress = ethereumRepository.getAddress()
        return ethereumSendRepository.getSendFee(userAddress, ethereumAddress, sendTokenMint, amount)
    }
}
