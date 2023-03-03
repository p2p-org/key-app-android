package org.p2p.ethereumkit

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import kotlinx.coroutines.test.runTest
import org.p2p.ethereumkit.external.EthereumModule
import org.p2p.ethereumkit.external.api.EthereumNetworkModule
import org.p2p.ethereumkit.external.balance.BalanceRepository
import org.p2p.ethereumkit.external.model.EthTokenKeyProvider
import org.p2p.ethereumkit.internal.core.EthereumKit

class TokenBalancesTest : KoinTest {

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
        val balance = repository.getTokenBalances(tokenKeyProvider.address)
        val stringBuilder = StringBuilder()
        balance.balances.map { it.contractAddress }.forEach { address ->
            val tokenMetadata = repository.getTokenMetadata(
                contractAddresses = address
            )
            val tokenName = tokenMetadata.tokenName
            val tokenSymbol = tokenMetadata.symbol
            val tokenDecimals = tokenMetadata.decimals
            val tokenLogo = tokenMetadata.logoUrl
            val tokenBalance = balance.balances.first { it.contractAddress == address }.tokenBalance
            stringBuilder.append("Token Name: $tokenName ")
            stringBuilder.append("Token Address: $address ")
            stringBuilder.append("Token Symbol: $tokenSymbol ")
            stringBuilder.append("Token Balance: $tokenBalance ")
            stringBuilder.append("Token Decimals: $tokenDecimals ")
            stringBuilder.append("Token logo: $tokenLogo ")
            stringBuilder.append("\n")
        }
        println(stringBuilder)
    }
}
