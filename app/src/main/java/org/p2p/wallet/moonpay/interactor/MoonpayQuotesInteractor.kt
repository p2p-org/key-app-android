package org.p2p.wallet.moonpay.interactor

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.moonpay.model.MoonpayBuyQuote
import org.p2p.wallet.moonpay.model.MoonpayBuyResult
import org.p2p.wallet.moonpay.repository.MoonpayRepository

private const val DEFAULT_AMOUNT = "1"
private const val DEFAULT_PAYMENT_TYPE = "credit_debit_card"

class MoonpayQuotesInteractor(
    private val moonpayRepository: MoonpayRepository,
    private val dispatchers: CoroutineDispatchers
) {

    private val quotes = mutableListOf<MoonpayBuyQuote>()

    fun getQuotes() = quotes.toList()

    suspend fun loadQuotes(currencies: List<String>, tokens: List<Token>) = withContext(dispatchers.io) {
        currencies.forEach { currency ->
            tokens.forEach { token ->
                launch { loadQuote(currency, token) }
            }
        }
    }

    private suspend fun loadQuote(currency: String, token: Token) {
        val response = moonpayRepository.getBuyCurrencyData(
            baseCurrencyAmount = DEFAULT_AMOUNT,
            quoteCurrencyAmount = null,
            tokenToBuy = token,
            baseCurrencyCode = currency.lowercase(),
            paymentMethod = DEFAULT_PAYMENT_TYPE
        )

        if (response is MoonpayBuyResult.Success) {
            quotes.add(
                MoonpayBuyQuote(
                    currency = currency,
                    token = token,
                    price = response.data.price,
                    minAmount = response.data.baseCurrency.minAmount
                )
            )
        }
    }
}
