package org.p2p.wallet.moonpay.interactor

import kotlinx.coroutines.withContext
import org.p2p.wallet.R
import org.p2p.wallet.common.feature_toggles.toggles.remote.BuyWithTransferFeatureToggle
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.moonpay.repository.NewMoonpayRepository

const val GBP_BANK_TRANSFER = "gbp_bank_transfer"
const val SEPA_BANK_TRANSFER = "sepa_bank_transfer"
const val CREDIT_DEBIT_CARD = "credit_debit_card"

private val CARD_PAYMENT_METHOD = PaymentMethod(
    method = PaymentMethod.MethodType.CARD,
    isSelected = false,
    feePercent = 4.5f,
    paymentPeriodResId = R.string.buy_period_card,
    methodResId = R.string.buy_method_card,
    iconResId = R.drawable.ic_card,
    paymentType = CREDIT_DEBIT_CARD
)

private val BANK_TRANSFER_PAYMENT_METHOD = PaymentMethod(
    method = PaymentMethod.MethodType.BANK_TRANSFER,
    isSelected = false,
    feePercent = 1f,
    paymentPeriodResId = R.string.buy_period_bank_transfer,
    methodResId = R.string.buy_method_bank_transfer,
    iconResId = R.drawable.ic_bank,
    paymentType = SEPA_BANK_TRANSFER
)

class PaymentMethodsInteractor(
    private val repository: NewMoonpayRepository,
    private val dispatchers: CoroutineDispatchers,
    private val bankTransferFeatureToggle: BuyWithTransferFeatureToggle
) {

    fun getAvailablePaymentMethods(alpha3Code: String): List<PaymentMethod> = when {
        bankTransferFeatureToggle.value && bankTransferIsAvailable(alpha3Code) -> {
            listOf(
                BANK_TRANSFER_PAYMENT_METHOD.copy(
                    paymentType = if (alpha3Code == BANK_TRANSFER_UK_CODE) {
                        GBP_BANK_TRANSFER
                    } else {
                        SEPA_BANK_TRANSFER
                    },
                    isSelected = true
                ),
                CARD_PAYMENT_METHOD.copy()
            )
        }
        else -> {
            listOf(CARD_PAYMENT_METHOD.copy(isSelected = true))
        }
    }

    private fun bankTransferIsAvailable(alpha3Code: String): Boolean = BANK_TRANSFER_ALPHA3_CODES.contains(alpha3Code)

    suspend fun getBankTransferAlphaCode(): String = withContext(dispatchers.io) {
        repository.getIpAddressData().alpha3
    }
}
