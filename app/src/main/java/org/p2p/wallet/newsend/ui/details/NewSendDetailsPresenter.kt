package org.p2p.wallet.newsend.ui.details

import android.content.res.Resources
import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.feerelayer.interactor.FeeRelayerCalculationInteractor
import org.p2p.wallet.newsend.model.SendSolanaFee
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.getErrorMessage

class NewSendDetailsPresenter(
    private val feeRelayerCalculationInteractor: FeeRelayerCalculationInteractor,
    private val userInteractor: UserInteractor,
    private val resources: Resources
) : BasePresenter<NewSendDetailsContract.View>(), NewSendDetailsContract.Presenter {

    override fun loadFeePayerTokens(fee: SendSolanaFee) {
        view?.showAccountCreationFeeLoading(isLoading = true)
        launch {
            try {
                val userTokens = userInteractor.getUserTokens()
                val feePayerTokens = feeRelayerCalculationInteractor.findAlternativeFeePayerTokens(
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
