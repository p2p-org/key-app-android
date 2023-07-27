package org.p2p.wallet.striga.offramp.withdraw

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.striga.offramp.withdraw.interactor.StrigaWithdrawInteractor

class StrigaWithdrawPresenter(
    private val validator: StrigaBankingDetailsValidator,
    private val interactor: StrigaWithdrawInteractor
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
                    StrigaWithdrawFragmentType.ConfirmEurWithdraw -> {
                        // no-op for now
                    }
                    is StrigaWithdrawFragmentType.ConfirmUsdcWithdraw -> {
                        interactor.withdrawUsdc(withdrawType.amountInUsdc)
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
}
