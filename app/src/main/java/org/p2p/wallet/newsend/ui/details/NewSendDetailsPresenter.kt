package org.p2p.wallet.newsend.ui.details

import android.content.res.Resources
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.send.interactor.SendInteractor
import org.p2p.wallet.send.model.SendSolanaFee
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.getErrorMessage
import timber.log.Timber
import kotlinx.coroutines.launch

class NewSendDetailsPresenter(
    private val sendInteractor: SendInteractor,
    private val userInteractor: UserInteractor,
    private val resources: Resources
) : BasePresenter<NewSendDetailsContract.View>(), NewSendDetailsContract.Presenter {

    override fun findAlternativeFeePayerTokens(fee: SendSolanaFee) {
        view?.showAccountCreationFeeLoading(isLoading = true)
        launch {
            try {
                val userTokens = userInteractor.getUserTokens()
                val feePayerTokens = sendInteractor.findAlternativeFeePayerTokens(userTokens, fee)
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
