package org.p2p.wallet.moonpay.interactor

import timber.log.Timber
import java.math.BigDecimal
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.isMoreThan
import org.p2p.wallet.infrastructure.network.interceptor.MoonpayRequestException
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayBuyCurrencyResponse
import org.p2p.wallet.moonpay.model.MoonpayBuyQuote
import org.p2p.wallet.moonpay.model.MoonpayBuyResult
import org.p2p.wallet.moonpay.repository.buy.MoonpayApiMapper
import org.p2p.wallet.moonpay.repository.buy.NewMoonpayBuyRepository
import org.p2p.wallet.moonpay.repository.sell.FiatCurrency

// amount in pounds does not accept getting currency rate of 1, it triggers minimal amount error
// so we use 100 as a workaround
private const val CURRENCY_AMOUNT_FOR_PRICE_REQUEST = "100"
private const val DEFAULT_MAX_CURRENCY_AMOUNT = 10000
private const val DEFAULT_PAYMENT_TYPE = "credit_debit_card"
private val FIAT_CURRENCY_CODES = listOf("eur", "usd", "gbp")

class BuyInteractor(
    private val moonpayRepository: NewMoonpayBuyRepository,
    private val moonpayApiMapper: MoonpayApiMapper,
    private val dispatchers: CoroutineDispatchers
) {

    companion object {
        const val HARDCODED_MIN_BUY_CURRENCY_AMOUNT = 40
    }

    private val quotes = mutableListOf<MoonpayBuyQuote>()

    fun getQuotes(): List<MoonpayBuyQuote> = quotes.toList()

    suspend fun loadQuotes(currencies: List<FiatCurrency>, tokens: List<Token>): Unit = withContext(dispatchers.io) {
        Timber.i("Loading quotes for buy: currencies=$currencies; tokens=${tokens.map(Token::mintAddress)}")
        currencies.flatMap { currency ->
            tokens.map { token ->
                async { loadQuote(currency, token) }
            }
        }
            .awaitAll()
    }

    fun getQuotesByCurrency(currency: FiatCurrency): List<MoonpayBuyQuote> {
        return quotes.filter { it.currency == currency }
    }

    suspend fun getMoonpayBuyResult(
        baseCurrencyAmount: String?,
        quoteCurrencyAmount: String?,
        tokenToBuy: Token,
        baseCurrencyCode: FiatCurrency,
        paymentMethod: String,
    ): MoonpayBuyResult {
        val minBuyAmount = getMinAmountForPair(baseCurrencyCode.abbriviation, tokenToBuy.tokenSymbol)

        return try {
            val response = moonpayRepository.getBuyCurrencyData(
                baseCurrencyAmount = baseCurrencyAmount,
                quoteCurrencyAmount = quoteCurrencyAmount,
                tokenToBuy = tokenToBuy,
                baseCurrencyCode = baseCurrencyCode.abbriviation,
                paymentMethod = paymentMethod
            )

            when {
                !isMinAmountValid(response, tokenToBuy.tokenSymbol) -> {
                    MoonpayBuyResult.MinAmountError(minBuyAmount)
                }
                !isMaxAmountValid(response) -> {
                    MoonpayBuyResult.MaxAmountError(DEFAULT_MAX_CURRENCY_AMOUNT.toBigDecimal())
                }
                else -> {
                    MoonpayBuyResult.Success(moonpayApiMapper.fromNetworkToDomain(response))
                }
            }
        } catch (error: MoonpayRequestException) {
            when {
                isMinimumAmountException(error) -> {
                    MoonpayBuyResult.MinAmountError(minBuyAmount)
                }
                error.httpCode == HttpsURLConnection.HTTP_BAD_REQUEST -> {
                    MoonpayBuyResult.Error(moonpayApiMapper.fromNetworkErrorToDomainMessage(error), cause = error)
                }
                else -> {
                    throw error
                }
            }
        }
    }

    private fun getMinAmountForPair(
        currencyCode: String,
        tokenSymbol: String
    ): BigDecimal {
        val quote = quotes.find {
            it.currency == FiatCurrency.getFromAbbreviation(currencyCode) && it.token.tokenSymbol == tokenSymbol
        }

        return HARDCODED_MIN_BUY_CURRENCY_AMOUNT.toBigDecimal()
    }

    private suspend fun loadQuote(currency: FiatCurrency, token: Token) {
        Timber.d("Load quote for currency=$currency; token=${token.tokenSymbol}")
        try {
            val response = moonpayRepository.getBuyCurrencyData(
                baseCurrencyAmount = CURRENCY_AMOUNT_FOR_PRICE_REQUEST,
                quoteCurrencyAmount = null,
                tokenToBuy = token,
                baseCurrencyCode = currency.abbriviation.lowercase(),
                paymentMethod = DEFAULT_PAYMENT_TYPE
            )
            val result = MoonpayBuyQuote(
                currency = currency,
                token = token,
                price = response.quoteCurrencyPrice,
                minAmount = HARDCODED_MIN_BUY_CURRENCY_AMOUNT.toBigDecimal()
            )
            quotes += result
        } catch (e: Throwable) {
            Timber.e(e, "Error while loading quote for currency=$currency; token=${token.tokenSymbol}")
        }
    }

    private fun isMinAmountValid(response: MoonpayBuyCurrencyResponse, tokenSymbol: String): Boolean {
        val buyCurrency = response.baseCurrency
        val isFiatCurrency = buyCurrency.code in FIAT_CURRENCY_CODES
        val minBuyAmount = getMinAmountForPair(response.baseCurrency.code, tokenSymbol)
        val isLessThenMin = response.totalAmount.isLessThan(minBuyAmount)
        return if (isFiatCurrency) !isLessThenMin else true
    }

    private fun isMaxAmountValid(response: MoonpayBuyCurrencyResponse): Boolean {
        val buyCurrency = response.baseCurrency
        val isFiatCurrency = buyCurrency.code in FIAT_CURRENCY_CODES
        val isMoreThenMax = response.totalAmount.isMoreThan(DEFAULT_MAX_CURRENCY_AMOUNT.toBigDecimal())
        return if (isFiatCurrency) !isMoreThenMax else true
    }

    private fun isMinimumAmountException(error: MoonpayRequestException): Boolean {
        return error.httpCode == HttpsURLConnection.HTTP_BAD_REQUEST && error.message.startsWith("Minimum purchase")
    }
}
