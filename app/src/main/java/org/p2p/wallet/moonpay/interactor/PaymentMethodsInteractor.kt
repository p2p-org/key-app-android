package org.p2p.wallet.moonpay.interactor

import kotlinx.coroutines.withContext
import org.p2p.wallet.R
import org.p2p.wallet.common.feature_toggles.toggles.remote.BuyWithTransferFeatureToggle
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.moonpay.model.Method
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.moonpay.repository.MoonpayRepository

private val CARD_PAYMENT_METHOD = PaymentMethod(
    method = Method.CARD,
    isSelected = false,
    feePercent = 4.5f,
    paymentPeriodResId = R.string.buy_period_card,
    methodResId = R.string.buy_method_card,
    iconResId = R.drawable.ic_card
)

private val BANK_TRANSFER_PAYMENT_METHOD = PaymentMethod(
    method = Method.BANK_TRANSFER,
    isSelected = false,
    feePercent = 1f,
    paymentPeriodResId = R.string.buy_period_bank_transfer,
    methodResId = R.string.buy_method_bank_transfer,
    iconResId = R.drawable.ic_bank
)

class PaymentMethodsInteractor(
    private val repository: MoonpayRepository,
    private val dispatchers: CoroutineDispatchers,
    private val bankTransferFeatureToggle: BuyWithTransferFeatureToggle
) {

    suspend fun getAvailablePaymentMethods(): List<PaymentMethod> = when {
        bankTransferFeatureToggle.value && bankTransferIsAvailable() -> {
            listOf(BANK_TRANSFER_PAYMENT_METHOD, CARD_PAYMENT_METHOD)
        }
        else -> {
            listOf(CARD_PAYMENT_METHOD)
        }
    }.apply { first().isSelected = true }

    private suspend fun bankTransferIsAvailable(): Boolean = withContext(dispatchers.io) {
        val moonpayIpAddressResponse = repository.getIpAddressData()
        val alpha3Code = moonpayIpAddressResponse.alpha3
        return@withContext BANK_TRANSFER_ALPHA3_CODES.contains(alpha3Code)
    }
}
