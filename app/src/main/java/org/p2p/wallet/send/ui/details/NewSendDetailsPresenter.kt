package org.p2p.wallet.send.ui.details

import android.content.res.Resources
import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.send.interactor.SendInteractor
import org.p2p.wallet.send.model.SendSolanaFee
import org.p2p.wallet.user.interactor.UserTokensInteractor
import org.p2p.wallet.utils.getErrorMessage

class NewSendDetailsPresenter(
    private val sendInteractor: SendInteractor,
    private val userInteractor: UserTokensInteractor,
    private val resources: Resources
) : BasePresenter<NewSendDetailsContract.View>(), NewSendDetailsContract.Presenter {

    override fun loadFeePayerTokens(fee: SendSolanaFee) {
        view?.showAccountCreationFeeLoading(isLoading = true)
        launch {
            try {
                val userTokens = userInteractor.getUserTokens()
                val feePayerTokens = sendInteractor.findAlternativeFeePayerTokens(
                    userTokens = userTokens,
                    feePayerToExclude = fee.feePayerToken,
                    transactionFeeInSOL = fee.feeRelayerFee.transactionFeeInSol,
                    accountCreationFeeInSOL = fee.feeRelayerFee.accountCreationFeeInSol
                )
                view?.showNoTokensScreen(feePayerTokens)
            } catch (e: Throwable) {
                Timber.e(e, "Error occurred while collecting fee payer tokens")
                view?.showUiKitSnackBar(e.getErrorMessage(resources::getString))
            } finally {
                view?.showAccountCreationFeeLoading(isLoading = false)
            }
        }
    }
}
