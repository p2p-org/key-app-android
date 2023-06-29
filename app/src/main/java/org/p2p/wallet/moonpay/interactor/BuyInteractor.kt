package org.p2p.wallet.moonpay.interactor

import timber.log.Timber
import java.math.BigDecimal
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.p2p.core.token.Token
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.isMoreThan
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.interceptor.MoonpayRequestException
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayBuyCurrencyResponse
import org.p2p.wallet.moonpay.model.MoonpayBuyQuote
import org.p2p.wallet.moonpay.model.MoonpayBuyResult
import org.p2p.wallet.moonpay.repository.buy.MoonpayApiMapper
import org.p2p.wallet.moonpay.repository.buy.NewMoonpayBuyRepository

private const val CURRENCY_AMOUNT_FOR_PRICE_REQUEST = "1"
private const val DEFAULT_MAX_CURRENCY_AMOUNT = 10000
private const val DEFAULT_PAYMENT_TYPE = "credit_debit_card"
private val FIAT_CURRENCY_CODES = listOf("eur", "usd", "gbp")

class BuyInteractor(
    private val moonpayRepository: NewMoonpayBuyRepository,
    private val moonpayApiMapper: MoonpayApiMapper,
    private val dispatchers: CoroutineDispatchers
) {

    companion object {
        const val DEFAULT_MIN_BUY_CURRENCY_AMOUNT = 40
    }

    private val quotes = mutableListOf<MoonpayBuyQuote>()

    fun getQuotes(): List<MoonpayBuyQuote> = quotes.toList()

    suspend fun loadQuotes(currencies: List<String>, tokens: List<Token>) = withContext(dispatchers.io) {
        Timber.i("Loading quotes for buy: currencies=$currencies; tokens=${tokens.map(Token::mintAddress)}")
        currencies.forEach { currency ->
            tokens.forEach { token ->
                launch { loadQuote(currency, token) }
            }
        }
    }

    fun getQuotesByCurrency(currency: String): List<MoonpayBuyQuote> {
        return quotes.filter { it.currency == currency }
    }

    suspend fun getMoonpayBuyResult(
        baseCurrencyAmount: String?,
        quoteCurrencyAmount: String?,
        tokenToBuy: Token,
        baseCurrencyCode: String,
        paymentMethod: String,
    ): MoonpayBuyResult {
        val minBuyAmount = getMinAmountForPair(baseCurrencyCode, tokenToBuy)

        return try {
            val response = moonpayRepository.getBuyCurrencyData(
                baseCurrencyAmount,
                quoteCurrencyAmount,
                tokenToBuy,
                baseCurrencyCode,
                paymentMethod
            )

            when {
                !isMinAmountValid(response, tokenToBuy) -> {
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

    private fun getMinAmountForPair(currency: String, token: Token): BigDecimal {
        val quote = quotes.find {
            it.currency.lowercase() == currency.lowercase() && it.token.tokenSymbol == token.tokenSymbol
        }

        return quote?.minAmount ?: DEFAULT_MIN_BUY_CURRENCY_AMOUNT.toBigDecimal()
    }

    private suspend fun loadQuote(currency: String, token: Token) {
        val response = moonpayRepository.getBuyCurrencyData(
            baseCurrencyAmount = CURRENCY_AMOUNT_FOR_PRICE_REQUEST,
            quoteCurrencyAmount = null,
            tokenToBuy = token,
            baseCurrencyCode = currency.lowercase(),
            paymentMethod = DEFAULT_PAYMENT_TYPE
        )

        quotes.add(
            MoonpayBuyQuote(
                currency = currency,
                token = token,
                price = response.quoteCurrencyPrice,
                minAmount = response.baseCurrency.minBuyAmount
            )
        )
    }

    private fun isMinAmountValid(response: MoonpayBuyCurrencyResponse, token: Token): Boolean {
        val buyCurrency = response.baseCurrency
        val isFiatCurrency = buyCurrency.code in FIAT_CURRENCY_CODES
        val minBuyAmount = getMinAmountForPair(response.baseCurrency.code, token)
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
