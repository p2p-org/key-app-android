package org.p2p.wallet.newsend.ui.details

import android.content.res.Resources
import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.newsend.interactor.SendInteractor
import org.p2p.wallet.newsend.model.SendSolanaFee
import org.p2p.wallet.utils.getErrorMessage
import timber.log.Timber
import org.p2p.wallet.user.interactor.UserTokensInteractor

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
                view?.showUiKitSnackBar(e.getErrorMessage { resources.getString(it) })
            } finally {
                view?.showAccountCreationFeeLoading(isLoading = false)
            }
        }
    }
}
