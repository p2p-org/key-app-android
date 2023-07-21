package org.p2p.wallet.striga.exchange.repository

import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.mockk
import org.intellij.lang.annotations.Language
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.math.BigDecimal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.p2p.core.utils.Constants
import org.p2p.wallet.striga.common.model.StrigaDataLayerError
import org.p2p.wallet.striga.common.model.StrigaDataLayerResult
import org.p2p.wallet.striga.exchange.api.StrigaExchangeApi
import org.p2p.wallet.striga.exchange.api.response.StrigaExchangeRateItemResponse
import org.p2p.wallet.striga.exchange.models.StrigaExchangePairsWithRates
import org.p2p.wallet.striga.exchange.models.StrigaExchangeRate
import org.p2p.wallet.striga.exchange.repository.impl.StrigaExchangeRemoteRepository
import org.p2p.wallet.striga.exchange.repository.mapper.StrigaExchangeRepositoryMapper
import org.p2p.wallet.utils.assertThat
import org.p2p.wallet.utils.fromJson

@OptIn(ExperimentalCoroutinesApi::class)
class StrigaExchangeRepositoryTest {

    private val gson = Gson()
    private val api: StrigaExchangeApi = mockk()

    private lateinit var repository: StrigaExchangeRepository

    @Language("JSON")
    private val exchangeRatesResponseBody = """
        {
         "ETHEUR":{"price":"1710.8","buy":"1719.36","sell":"1702.24","timestamp":1689587021793,"currency":"Euros"},
         "USDCEUR":{"price":"0.89","buy":"0.9","sell":"0.88","timestamp":1689587019000,"currency":"Euros"},
         "USDCUSDT":{"price":"1","buy":"1.01","sell":"0.99","timestamp":1689587019000,"currency":"Tether"},
         "USDTEUR":{"price":"0.89","buy":"0.9","sell":"0.88","timestamp":1689587022608,"currency":"Euros"},
         "BTCEUR":{"price":"26892","buy":"27026.46","sell":"26757.54","timestamp":1689587022411,"currency":"Euros"},
         "BTCUSDC":{"price":"30236","buy":"30387.18","sell":"30084.82","timestamp":1689587020000,"currency":"USD Coin"},
         "BTCUSDT":{"price":"30216","buy":"30367.08","sell":"30064.92","timestamp":1689587022609,"currency":"Tether"},
         "BUSDEUR":{"price":"1.13","buy":"1.13","sell":"1.11","timestamp":1689587021246,"currency":"Binance USD"},
         "BNBEUR":{"price":"216.7","buy":"217.79","sell":"215.61","timestamp":1689587022323,"currency":"Euros"},
         "LINKBUSD":{"price":"6.62","buy":"6.66","sell":"6.58","timestamp":1689587020408,"currency":"Binance USD"},
         "MATICBUSD":{"price":"0.78","buy":"0.78","sell":"0.76","timestamp":1689587022494,"currency":"Binance USD"},
         "SUSHIBUSD":{"price":"0.78","buy":"0.78","sell":"0.77","timestamp":1689587022347,"currency":"Binance USD"},
         "UNIBUSD":{"price":"5.94","buy":"5.97","sell":"5.9","timestamp":1689587020099,"currency":"Binance USD"},
         "1INCHBUSD":{"price":"0.51","buy":"0.51","sell":"0.5","timestamp":1689587022511,"currency":"Binance USD"}
        }
    """.trimIndent()

    private val expectedUsdcEuroRate = StrigaExchangeRate(
        priceUsd = BigDecimal("0.89"),
        buyRate = BigDecimal("0.9"),
        sellRate = BigDecimal("0.88"),
        timestamp = 1689587019000L,
        currencyName = "Euros"
    )

    @Before
    fun setUp() {
        repository = StrigaExchangeRemoteRepository(
            api = api,
            mapper = StrigaExchangeRepositoryMapper(),
        )

        val obj = exchangeRatesResponseBody.fromJson<Map<String, StrigaExchangeRateItemResponse>>(gson)
        coEvery { api.getExchangeRates() } returns obj
        val jsonBack = gson.toJson(obj)
        println(jsonBack)
    }

    @Test
    fun `GIVEN exchange rates error WHEN getExchangeRates THEN check result is error`() = runTest {
        coEvery { api.getExchangeRates() } throws HttpException(mockk(relaxed = true))
        val result = repository.getExchangeRates()
        result.assertThat()
            .isInstanceOf(StrigaDataLayerResult.Failure::class.java)
    }

    @Test
    fun `GIVEN exchange rates WHEN findRate with arbitrary order of symbols THEN it returns the same rates`() = runTest {
        val result = repository.getExchangeRates()
        result.assertThat()
            .isInstanceOf(StrigaDataLayerResult.Success::class.java)

        result as StrigaDataLayerResult.Success<StrigaExchangePairsWithRates>

        result.value.findRate(Constants.EUR_SYMBOL, Constants.USDC_SYMBOL).assertThat()
            .isNotNull()
            .isEqualTo(expectedUsdcEuroRate)

        result.value.findRate(Constants.USDC_SYMBOL, Constants.EUR_SYMBOL).assertThat()
            .isNotNull()
            .isEqualTo(expectedUsdcEuroRate)
    }

    @Test
    fun `GIVEN exchange rates WHEN find nonexistent rate THEN findRate returns null`() = runTest {
        val result = repository.getExchangeRates()

        result as StrigaDataLayerResult.Success<StrigaExchangePairsWithRates>

        result.value.findRate("UNIBUSD", Constants.EUR_SYMBOL).assertThat()
            .isNull()
    }

    @Test
    fun `GIVEN exchange rates WHEN given a rate THEN hasRate returns true`() = runTest {
        val result = repository.getExchangeRates()

        result as StrigaDataLayerResult.Success<StrigaExchangePairsWithRates>

        result.value.hasRate(Constants.EUR_SYMBOL, Constants.USDC_SYMBOL)
            .assertThat()
            .isTrue()

        result.value.hasRate(Constants.USDC_SYMBOL, Constants.EUR_SYMBOL)
            .assertThat()
            .isTrue()
    }

    @Test
    fun `GIVEN exchange rates WHEN getAvailablePairsForToken THEN check all supported tokens are presented`() =
        runTest {
            val result = repository.getExchangeRates()

            result as StrigaDataLayerResult.Success<StrigaExchangePairsWithRates>

            // we don't see other pairs because we don't support them
            result.value.getAvailablePairsForToken(Constants.EUR_SYMBOL).assertThat()
                .containsExactlyInAnyOrder(
                    Constants.ETH_SYMBOL to Constants.EUR_SYMBOL,
                    Constants.USDC_SYMBOL to Constants.EUR_SYMBOL,
                    Constants.USDT_SYMBOL to Constants.EUR_SYMBOL,
                    Constants.BTC_SYMBOL to Constants.EUR_SYMBOL,
                )
        }

    @Test
    fun `GIVE exchange rates WHEN getExchangeRateForPair THEN check result has a rate`() = runTest {
        val result = repository.getExchangeRateForPair(Constants.EUR_SYMBOL, Constants.USDC_SYMBOL)

        result as StrigaDataLayerResult.Success<StrigaExchangeRate>

        result.value.assertThat()
            .isEqualTo(expectedUsdcEuroRate)
    }

    @Test
    fun `GIVE exchange rates WHEN getExchangeRateForPair for nonexistent pair THEN check result has no rate`() = runTest {
        val result = repository.getExchangeRateForPair("ABRAKADABRA", Constants.USDC_SYMBOL)

        result.assertThat()
            .isInstanceOf(StrigaDataLayerResult.Failure::class.java)

        result as StrigaDataLayerResult.Failure<StrigaExchangeRate>
        result.error.assertThat()
            .isInstanceOf(StrigaDataLayerError.InternalError::class.java)

        result.error.cause.assertThat()
            .isNotNull()
            .isInstanceOf(StrigaExchangeRemoteRepository.StrigaExchangeRateNotFound::class.java)
    }
}
