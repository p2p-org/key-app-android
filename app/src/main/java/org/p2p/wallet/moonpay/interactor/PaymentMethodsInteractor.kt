package org.p2p.wallet.moonpay.interactor

import org.p2p.wallet.R
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.moonpay.repository.buy.NewMoonpayBuyRepository

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

class PaymentMethodsInteractor(private val repository: NewMoonpayBuyRepository) {

    fun getAvailablePaymentMethods(
        alpha3Code: String,
        preselectedMethodType: PaymentMethod.MethodType?
    ): List<PaymentMethod> {
        return if (bankTransferIsAvailable(alpha3Code)) {
            val validatedBankTransfer = BANK_TRANSFER_PAYMENT_METHOD.copy(
                paymentType = if (alpha3Code == BANK_TRANSFER_UK_CODE) GBP_BANK_TRANSFER else SEPA_BANK_TRANSFER,
                isSelected = if (preselectedMethodType == null) {
                    true
                } else {
                    preselectedMethodType == PaymentMethod.MethodType.BANK_TRANSFER
                }
            )
            val cardPayment = CARD_PAYMENT_METHOD.copy(
                isSelected = preselectedMethodType == PaymentMethod.MethodType.CARD
            )
            listOf(
                validatedBankTransfer, cardPayment
            )
        } else {
            listOf(CARD_PAYMENT_METHOD.copy(isSelected = true))
        }
    }

    private fun bankTransferIsAvailable(alpha3Code: String): Boolean = BANK_TRANSFER_ALPHA3_CODES.contains(alpha3Code)

    suspend fun getBankTransferAlphaCode(): String = repository.getIpAddressData().currentCountryAbbreviation
}
