package org.p2p.wallet.moonpay.interactor

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.data.ErrorCode
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.moonpay.api.MoonpayBuyCurrencyResponse
import org.p2p.wallet.moonpay.model.MoonpayBuyQuote
import org.p2p.wallet.moonpay.model.MoonpayBuyResult
import org.p2p.wallet.moonpay.repository.MoonpayApiMapper
import org.p2p.wallet.moonpay.repository.NewMoonpayRepository
import org.p2p.wallet.utils.isLessThan
import java.math.BigDecimal

const val DEFAULT_MIN_AMOUNT = 40

private const val DEFAULT_AMOUNT = "1"
private const val DEFAULT_PAYMENT_TYPE = "credit_debit_card"
private val FIAT_CURRENCY_CODES = listOf("eur", "usd", "gbp")

class MoonpayBuyInteractor(
    private val moonpayRepository: NewMoonpayRepository,
    private val moonpayApiMapper: MoonpayApiMapper,
    private val dispatchers: CoroutineDispatchers
) {

    private val quotes = mutableListOf<MoonpayBuyQuote>()

    fun getQuotes(): List<MoonpayBuyQuote> = quotes.toList()

    suspend fun loadQuotes(currencies: List<String>, tokens: List<Token>) = withContext(dispatchers.io) {
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

        try {
            val response = moonpayRepository.getBuyCurrencyData(
                baseCurrencyAmount,
                quoteCurrencyAmount,
                tokenToBuy,
                baseCurrencyCode,
                paymentMethod
            )

            return if (isMinimumAmountValid(response, tokenToBuy)) {
                MoonpayBuyResult.Success(moonpayApiMapper.fromNetworkToDomain(response))
            } else {
                MoonpayBuyResult.MinimumAmountError(minBuyAmount)
            }
        } catch (error: ServerException) {
            return when {
                isMinimumAmountException(error) -> {
                    MoonpayBuyResult.MinimumAmountError(minBuyAmount)
                }
                error.errorCode == ErrorCode.BAD_REQUEST -> {
                    MoonpayBuyResult.Error(moonpayApiMapper.fromNetworkErrorToDomainMessage(error))
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

        return quote?.minAmount ?: DEFAULT_MIN_AMOUNT.toBigDecimal()
    }

    private suspend fun loadQuote(currency: String, token: Token) {
        val response = moonpayRepository.getBuyCurrencyData(
            baseCurrencyAmount = DEFAULT_AMOUNT,
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

    private fun isMinimumAmountValid(response: MoonpayBuyCurrencyResponse, token: Token): Boolean {
        val buyCurrency = response.baseCurrency
        val isFiatCurrency = buyCurrency.code in FIAT_CURRENCY_CODES
        val minBuyAmount = getMinAmountForPair(response.baseCurrency.code, token)
        val isLessThen = response.totalAmount.isLessThan(minBuyAmount)
        return if (isFiatCurrency) !isLessThen else true
    }

    private fun isMinimumAmountException(error: ServerException): Boolean {
        return error.errorCode == ErrorCode.BAD_REQUEST &&
            error.getDirectMessage()?.startsWith("Minimum purchase") == true
    }
}
