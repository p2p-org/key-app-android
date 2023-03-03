package org.p2p.ethereumkit.external.repository

import java.math.BigInteger
import org.p2p.ethereumkit.external.balance.BalanceRepository
import org.p2p.ethereumkit.external.model.EthTokenKeyProvider
import org.p2p.ethereumkit.internal.core.EthereumKit

internal class EthereumKitRepository(
    private val tokenKeyProvider: EthTokenKeyProvider,
    private val balanceRepository: BalanceRepository
) : EthereumRepository {

    init {
        EthereumKit.init()
    }

    override suspend fun getBalance(): BigInteger {
        return balanceRepository.getWalletBalance(tokenKeyProvider.address)
    }
}
