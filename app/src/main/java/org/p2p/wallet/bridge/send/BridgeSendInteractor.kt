package org.p2p.wallet.bridge.send

import org.p2p.wallet.bridge.send.repository.EthereumSendRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class BridgeSendInteractor(
    private val repository: EthereumSendRepository,
    private val ethereumKitRepository: EthereumSendRepository,
    private val tokenKeyProvider: TokenKeyProvider,
)
