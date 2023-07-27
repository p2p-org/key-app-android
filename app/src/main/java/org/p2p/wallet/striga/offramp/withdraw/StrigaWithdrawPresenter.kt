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
    private val resources: Resources
) : BasePresenter<StrigaWithdrawContract.View>(),
    StrigaWithdrawContract.Presenter {

    private var enteredIban: String = ""
    private var enteredBic: String = ""

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
        enteredIban = newIban
        view?.showIbanValidationResult(validator.validateIban(newIban))
    }

    override fun onBicChanged(newBic: String) {
        enteredBic = newBic
        view?.showBicValidationResult(validator.validateBic(newBic))
    }

    override fun withdraw(withdrawType: StrigaWithdrawFragmentType) {
        launch {
            try {
                view?.showLoading(isLoading = true)

                when (withdrawType) {
                    is StrigaWithdrawFragmentType.ConfirmUsdcWithdraw -> {
                        val (internalTransactionId, usdcToken) = interactor.withdrawUsdc(withdrawType.amountInUsdc)
                        view?.navigateToTransactionDetails(
                            transactionId = internalTransactionId,
                            data = buildTransactionDetails(usdcToken, withdrawType.amountInUsdc)
                        )
                    }
                    is StrigaWithdrawFragmentType.ConfirmEurWithdraw -> {
                        val challengeId = interactor.withdrawEur(withdrawType.amountEur)
                        view?.navigateToOtpConfirm(
                            challengeId = challengeId,
                            amountToOffRamp = withdrawType.amountEur
                        )
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

    private fun buildTransactionDetails(
        usdcToken: Token.Active,
        usdcAmount: BigDecimal,
    ): NewShowProgress {
        return NewShowProgress(
            date = ZonedDateTime.now(),
            tokenUrl = usdcToken.iconUrl.orEmpty(),
            amountTokens = usdcAmount.toPlainString(),
            amountUsd = null,
            data = listOf(
                TitleValue(resources.getString(R.string.striga_withdrawal_transaction_iban), enteredIban),
                TitleValue(resources.getString(R.string.striga_withdrawal_transaction_bic), enteredBic)
            )
        )
    }
}
