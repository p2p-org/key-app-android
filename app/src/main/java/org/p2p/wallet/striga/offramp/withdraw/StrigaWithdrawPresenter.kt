package org.p2p.wallet.striga.offramp.withdraw

import kotlinx.coroutines.launch
import org.p2p.wallet.R
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
                val offRampCredentials = strigaWalletInteractor.getEurAccountStatement()
                enteredIban = offRampCredentials.bankingIban.orEmpty()
                enteredBic = offRampCredentials.bankingBic.orEmpty()

                view.showBankingDetails(offRampCredentials)
            } catch (error: Throwable) {
                view.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }
    }

    override fun onIbanChanged(newIban: String) {
        view?.showIbanIsValid(validator.validateIban(newIban))
    }

    override fun onBicChanged(newBic: String) {
        view?.showBicIsValid(validator.validateBic(newBic))
    }
}
