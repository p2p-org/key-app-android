package org.p2p.ethereumkit

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import org.web3j.crypto.TransactionDecoder
import kotlinx.coroutines.test.runTest
import org.p2p.core.rpc.RPC_RETROFIT_QUALIFIER
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.core.wrapper.eth.toHexString
import org.p2p.ethereumkit.external.api.EthereumNetworkModule
import org.p2p.ethereumkit.external.core.CoroutineDispatchers
import org.p2p.ethereumkit.external.repository.EthereumRepository
import org.p2p.ethereumkit.internal.core.EthereumKit
import org.p2p.ethereumkit.internal.core.TransactionBuilder
import org.p2p.ethereumkit.internal.core.signer.Signer
import org.p2p.ethereumkit.internal.models.Chain
import org.p2p.ethereumkit.internal.models.GasPrice
import org.p2p.ethereumkit.internal.models.RawTransaction

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
            single(named(RPC_RETROFIT_QUALIFIER)) { EthereumNetworkModule.getRetrofit(get(), get()) }
            single<CoroutineDispatchers> { TestCoroutineDispatcher() }
        }
    }

    @Test
    fun getBalance() = runTest {
        repository.init(seedPhrase = "apart approve black comfort steel spin real renew tone primary key cherry".split(" "))
    }
}

