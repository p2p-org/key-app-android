package org.p2p.wallet.utils

import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.logger.Level
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.p2p.ethereumkit.external.EthereumModule
import org.p2p.ethereumkit.external.api.EthereumNetworkModule
import org.p2p.ethereumkit.external.balance.BalanceRepository
import org.p2p.ethereumkit.external.model.EthTokenKeyProvider
import org.p2p.ethereumkit.internal.core.EthereumKit
import org.p2p.wallet.R
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.dispatchers.DefaultDispatchers
import org.p2p.wallet.user.repository.prices.TokenAddress
import org.p2p.wallet.user.repository.prices.TokenPricesRemoteRepository
import org.p2p.wallet.user.repository.prices.di.TokenPricesModule

class TokenBalancesAndPricesTest : KoinTest {

    private val balanceRepository: BalanceRepository by inject()
    private val tokenKeyProvider: EthTokenKeyProvider by inject()
    private val tokenPricesRepository: TokenPricesRemoteRepository by inject()

    private val featureFlags: InAppFeatureFlags = mockk {
        every { useCoinGeckoForPrices.featureValue }.returns(true)
    }

    private val mockContext = MockUtils.mockContext {
        every { getString(eq(R.string.coinGeckoBaseUrl)) }.returns("https://api.coingecko.com/api/v3/")
    }

    private val mockApplication = MockUtils.mockApplication(mockContext)

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        EthereumKit.init()
    }

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger(Level.DEBUG)
        modules(
            module {
                androidContext(mockApplication)
                androidContext(mockContext)
                single { MockUtils.sharedPrefsMock }
                single { featureFlags }
                singleOf(::DefaultDispatchers) bind CoroutineDispatchers::class
            },
            TokenPricesModule.create(),
            EthereumNetworkModule.create(),
            EthereumModule.create()
        )
    }

    @Test
    fun getBalanceAndPrices() = runTest {
        val stringBuilder = StringBuilder()
        val tokenBalancesResponse = balanceRepository.getTokenBalances(tokenKeyProvider.address)
        val tokenPriceMap = tokenPricesRepository.getTokenPricesByAddressesMap(
            tokenBalancesResponse.balances.map { TokenAddress(it.contractAddress.toString()) }
        )
        tokenBalancesResponse.balances.forEach { balanceResponse ->
            val tokenAddress = balanceResponse.contractAddress
            val tokenMetadataResponse = balanceRepository.getTokenMetadata(
                contractAddresses = tokenAddress
            )
            val tokenMetadata = TokenConverter.fromNetwork(tokenAddress.toString(), tokenMetadataResponse)
            val tokenPrice = tokenPriceMap[TokenAddress(tokenAddress.toString())]

            stringBuilder.append("Token Metadata: $tokenMetadata ")
            stringBuilder.append("Token Price: $tokenPrice ")
            stringBuilder.append("\n")
        }
        println(stringBuilder)
    }

    @After
    fun after() {
        Dispatchers.resetMain()
    }
}
