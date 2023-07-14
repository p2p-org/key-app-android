package org.p2p.wallet.home.ui.wallet.handlers

import timber.log.Timber
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.home.ui.main.delegates.striga.onramp.StrigaOnRampCellModel
import org.p2p.wallet.home.ui.wallet.WalletContract
import org.p2p.wallet.striga.onramp.interactor.StrigaOnRampInteractor

class StrigaOnRampClickHandler(
    private val strigaOnRampInteractor: StrigaOnRampInteractor
) {
    suspend fun handle(view: WalletContract.View?, item: StrigaOnRampCellModel) {
        try {
            view?.showStrigaOnRampProgress(isLoading = true, tokenMint = item.tokenMintAddress)
            val challengeId = strigaOnRampInteractor.onRampToken(item.amountAvailable, item.payload).unwrap()
            view?.navigateToStrigaOnRampConfirmOtp(challengeId, item)
        } catch (e: Throwable) {
            Timber.e(e, "Error on claiming striga token")
            if (BuildConfig.DEBUG) {
                view?.showErrorMessage(IllegalStateException("Striga claiming is not supported yet", e))
            } else {
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        } finally {
            view?.showStrigaOnRampProgress(isLoading = false, tokenMint = item.tokenMintAddress)
        }
    }
}
