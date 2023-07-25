package org.p2p.wallet.striga.offramp.withdraw

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor

class StrigaWithdrawPresenter(
    private val strigaWalletInteractor: StrigaWalletInteractor,
    private val validator: StrigaBankingDetailsValidator
) : BasePresenter<StrigaWithdrawContract.View>(),
    StrigaWithdrawContract.Presenter {

    private var enteredIban: String = ""
    private var enteredBic: String = ""

    override fun attach(view: StrigaWithdrawContract.View) {
        super.attach(view)
        launch {
            try {
                val offRampCredentials = strigaWalletInteractor.getEurBankingDetails()
                enteredIban = offRampCredentials.bankingIban.orEmpty()
                enteredBic = offRampCredentials.bankingBic.orEmpty()

                view.showPrefilledBankingDetails(offRampCredentials)
            } catch (error: Throwable) {
                Timber.e(error, "Failed to fetch off-ramp credentials")
            }
        }
    }

    override fun onIbanChanged(newIban: String) {
        view?.showIbanValidationResult(validator.validateIban(newIban))
    }

    override fun onBicChanged(newBic: String) {
        view?.showBicValidationResult(validator.validateBic(newBic))
    }

    override fun withdraw(withdrawType: StrigaWithdrawFragmentType) {
        launch {
            try {
                view?.showLoading(isLoading = true)
                // make request to SEPA OR Solana blockchain

                strigaWalletInteractor.saveNewEurBankingDetails(
                    userBic = enteredBic,
                    userIban = enteredIban
                )
            } catch (error: Throwable) {
                Timber.e(error, "Failed to make withdrawal")
            }
            view?.showLoading(isLoading = false)
        }
    }
}
