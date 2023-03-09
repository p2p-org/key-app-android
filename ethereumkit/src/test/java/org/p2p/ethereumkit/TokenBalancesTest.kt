package org.p2p.ethereumkit

import kotlinx.coroutines.delay
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import kotlinx.coroutines.test.runTest
import org.koin.dsl.module
import org.p2p.ethereumkit.external.core.CoroutineDispatchers
import org.p2p.ethereumkit.external.repository.EthereumRepository
import org.p2p.ethereumkit.internal.core.EthereumKit
import kotlin.test.assertIs

class TokenBalancesTest : KoinTest {

    private val repository: EthereumRepository by inject()

    @Before
    fun setup() {
        EthereumKit.init()
    }

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        allowOverride(true)
        printLogger(Level.DEBUG)
        modules(
            EthereumKitService.getEthereumKitModules(),
        )
        module {
            single<CoroutineDispatchers> { TestCoroutineDispatcher() }
        }
    }

    @Test
    fun getBalance() = runTest {
        repository.init(seedPhrase = "apart approve black comfort steel spin real renew tone primary key cherry".split(" "))
        val result = repository.loadWalletTokens()
        println(result)
        assert(result.isNotEmpty())
    }
}

