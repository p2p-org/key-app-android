package org.p2p.wallet.striga.offramp.withdraw

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.striga.wallet.models.StrigaUserBankingDetails

interface StrigaWithdrawContract {
    interface View : MvpView {
        fun showBankingDetails(offRampCredentials: StrigaUserBankingDetails)

        fun showIbanIsValid(validationResult: StrigaWithdrawValidationResult)
        fun showBicIsValid(validationResult: StrigaWithdrawValidationResult)
    }

    interface Presenter : MvpPresenter<View> {
        fun onBicChanged(newBic: String)
        fun onIbanChanged(newIban: String)
    }
}
