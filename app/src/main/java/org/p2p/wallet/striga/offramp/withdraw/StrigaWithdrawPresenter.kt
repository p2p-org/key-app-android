package org.p2p.wallet.striga.offramp.withdraw

import android.content.res.Resources
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.launch
import org.p2p.core.model.TitleValue
import org.p2p.core.token.Token
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.striga.offramp.withdraw.interactor.StrigaWithdrawInteractor
import org.p2p.wallet.transaction.model.NewShowProgress

class StrigaWithdrawPresenter(
    private val validator: StrigaBankingDetailsValidator,
    private val interactor: StrigaWithdrawInteractor,
    private val resources: Resources,
) : BasePresenter<StrigaWithdrawContract.View>(),
    StrigaWithdrawContract.Presenter {

    private var enteredIban: String = ""
    private var enteredBic: String = ""

    private var isValidationValid: Boolean = false

    override fun attach(view: StrigaWithdrawContract.View) {
        super.attach(view)
        launch {
            try {
                val offRampCredentials = interactor.getUserEurBankingDetails()
                view.showPrefilledBankingDetails(offRampCredentials)
            } catch (error: Throwable) {
                Timber.e(error, "Failed to fetch off-ramp credentials")
            }
        }
    }

    override fun onIbanChanged(newIban: String) {
        enteredIban = newIban.replace(" ", "")

        val validationResult = validator.validateIban(enteredIban)
        isValidationValid = validationResult == StrigaWithdrawValidationResult.VALUE_IS_VALID
        view?.showIbanValidationResult(validationResult)
    }

    override fun onBicChanged(newBic: String) {
        enteredBic = newBic

        val validationResult = validator.validateBic(newBic)
        isValidationValid = validationResult == StrigaWithdrawValidationResult.VALUE_IS_VALID
        view?.showBicValidationResult(validationResult)
    }

    override fun withdraw(withdrawType: StrigaWithdrawFragmentType) {
        if (!areFieldValidToWithdraw()) return

        launch {
            try {
                view?.showLoading(isLoading = true)

                when (withdrawType) {
                    is StrigaWithdrawFragmentType.ConfirmUsdcWithdraw -> {
                        val (internalTransactionId, usdcToken) =
                            interactor.withdrawUsdc(withdrawType.amountInUsdc)

                        view?.navigateToTransactionDetails(
                            transactionId = internalTransactionId,
                            data = buildTransactionDetails(usdcToken, withdrawType.amountInUsdc)
                        )
                    }
                    is StrigaWithdrawFragmentType.ConfirmEurWithdraw -> {
                        val challengeId = interactor.withdrawEur(withdrawType.amountEur)
                        view?.navigateToOtpConfirm(challengeId)
                    }
                }

                interactor.saveUpdatedEurBankingDetails(enteredBic, enteredIban)
            } catch (error: Throwable) {
                Timber.e(error, "Failed to make withdrawal for $withdrawType")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
            view?.showLoading(isLoading = false)
        }
    }

    private fun areFieldValidToWithdraw(): Boolean {
        val areFieldsValid: Boolean = listOf(
            validator.validateBic(enteredBic),
            validator.validateIban(enteredIban)
        )
            .all { it == StrigaWithdrawValidationResult.VALUE_IS_VALID }
        if (!areFieldsValid) {
            // trigger field change to show error
            onIbanChanged(enteredIban)
            onBicChanged(enteredBic)
        }
        return areFieldsValid
    }

    private fun buildTransactionDetails(
        usdcToken: Token.Active,
        usdcAmount: BigDecimal,
    ): NewShowProgress {
        return NewShowProgress(
            date = ZonedDateTime.now(),
            tokenUrl = usdcToken.iconUrl.orEmpty(),
            amountTokens = usdcAmount.toPlainString(),
            amountUsd = null,
            transactionDetails = listOf(
                TitleValue(resources.getString(R.string.striga_withdraw_transaction_iban), enteredIban),
                TitleValue(resources.getString(R.string.striga_withdraw_transaction_bic), enteredBic)
            )
        )
    }
}
