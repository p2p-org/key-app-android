package org.p2p.ethereumkit

import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import org.p2p.ethereumkit.external.EthereumModule
import org.p2p.ethereumkit.external.api.EthereumNetworkModule
import org.p2p.ethereumkit.external.balance.BalanceRepository
import org.p2p.ethereumkit.external.model.EthTokenKeyProvider
import org.p2p.ethereumkit.internal.core.EthereumKit

class EthereumBalanceTest : KoinTest {

    private val repository: BalanceRepository by inject()
    private val tokenKeyProvider: EthTokenKeyProvider by inject()

    @Before
    fun setup() {
        EthereumKit.init()
    }
    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger(Level.DEBUG)
        modules(
            EthereumNetworkModule.create(),
            EthereumModule.create()
        )
    }

    @Test
    fun getBalance() = runTest {
        val balance = repository.getWalletBalance(tokenKeyProvider.address)
    }
}
