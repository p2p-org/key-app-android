package org.p2p.token.service.repository.price

import assertk.all
import assertk.assertions.hasSize
import assertk.assertions.index
import assertk.assertions.isNull
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.p2p.core.utils.Constants
import org.p2p.token.service.database.TokenPriceDao
import org.p2p.token.service.database.mapper.TokenServiceDatabaseMapper
import org.p2p.token.service.model.TokenRate
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.wallet.utils.assertThat
import org.p2p.wallet.utils.isEven

@OptIn(ExperimentalCoroutinesApi::class)
class TokenPriceDatabaseRepositoryTest {
    private lateinit var repository: TokenPriceDatabaseRepository
    private var tokenPriceDao: TokenPriceDao = InMemoryTokenPriceDao()

    @Before
    fun setUp() {
        tokenPriceDao = InMemoryTokenPriceDao()
        repository = TokenPriceDatabaseRepository(
            tokenPriceDao = tokenPriceDao,
            converter = TokenServiceDatabaseMapper()
        )
    }

    @Test
    fun `GIVEN prices with rates WHEN save THEN all is saved`() = runTest {
        // GIVEN
        val pricesToSave = generateDomainPrices()
        // WHEN
        repository.saveTokensPrice(pricesToSave)
        // THEN
        val savedPrices = repository.getLocalTokenPrices()
        savedPrices.assertThat()
            .all { hasSize(pricesToSave.size) }
    }

    @Test
    fun `GIVEN some prices with no rates WHEN save THEN all is saved`() = runTest {
        // GIVEN
        val pricesToSave = generateDomainPrices().mapIndexed { i, price ->
            if (i.isEven()) price.copy(rate = TokenRate(usd = null)) else price
        }
        val pricesWithRates = pricesToSave.filter { it.rate.usd != null }
        // WHEN
        repository.saveTokensPrice(pricesToSave)
        // THEN
        val savedPrices = repository.getLocalTokenPrices()
        savedPrices.assertThat()
            .all { hasSize(pricesWithRates.size) }
    }

    @Test
    fun `GIVEN some new prices WHEN save THEN flow is triggered with new values`() = runTest {
        // GIVEN
        val priceEmissions = mutableListOf<List<TokenServicePrice>>()

        TestScope().launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeTokenPrices(TokenServiceNetwork.SOLANA)
                .toList(priceEmissions)
        }

        val initialPricesSize = 2
        val pricesToSave = generateDomainPrices().map {
            it.copy(network = TokenServiceNetwork.SOLANA)
        }
        repository.saveTokensPrice(pricesToSave.take(initialPricesSize))
        // WHEN
        repository.saveTokensPrice(pricesToSave)
        // THEN
        val totalEmissionsExpected = 3
        priceEmissions.assertThat().all {
            hasSize(totalEmissionsExpected)
            index(1).hasSize(initialPricesSize)
            index(2).hasSize(pricesToSave.size)
        }
    }

    @Test
    fun `GIVEN not saved token address WHEN find in local THEN return null`() = runTest {
        // GIVEN
        val pricesToSave = generateDomainPrices()
        repository.saveTokensPrice(pricesToSave)
        // WHEN
        val actualFindPrice = repository.findTokenPriceByAddress(
            address = "some_random_mint",
            networkChain = TokenServiceNetwork.SOLANA
        )
        // THEN
        actualFindPrice.assertThat()
            .isNull()
    }

    private fun generateDomainPrices(): List<TokenServicePrice> {
        return buildList {
            repeat(10) {
                val randomNetwork = if (it.rem(2) == 0) {
                    TokenServiceNetwork.SOLANA
                } else {
                    TokenServiceNetwork.ETHEREUM
                }
                this += TokenServicePrice(
                    tokenAddress = Constants.SOL_MINT + it,
                    rate = TokenRate(usd = it.toLong().toBigDecimal()),
                    network = randomNetwork,
                )
            }
        }
    }
}
