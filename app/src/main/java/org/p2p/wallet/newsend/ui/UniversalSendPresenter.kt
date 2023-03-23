package org.p2p.wallet.newsend.ui

import android.content.res.Resources
import java.math.BigDecimal
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.core.utils.isZero
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.newsend.statemachine.SendFeatureAction
import org.p2p.wallet.newsend.statemachine.SendState
import org.p2p.wallet.newsend.statemachine.SendStateMachine
import org.p2p.wallet.newsend.statemachine.lastStaticState
import org.p2p.wallet.newsend.statemachine.model.SendToken
import org.p2p.wallet.newsend.statemachine.tokenActive
import org.p2p.wallet.newsend.ui.relay.SendUiRelay
import org.p2p.wallet.newsend.ui.sendtransaction.SendTransactionDelegate
import org.p2p.wallet.updates.ConnectionStateProvider
import org.p2p.wallet.user.interactor.UserInteractor

class UniversalSendPresenter constructor(
    private val userInteractor: UserInteractor,
    private val resources: Resources,
    private val connectionStateProvider: ConnectionStateProvider,

    // Universal staff
    private val uiRelay: SendUiRelay,
    private val sendTransactionDelegate: SendTransactionDelegate,
    private val stateMachine: SendStateMachine,
) : BasePresenter<NewSendContract.View>(), NewSendContract.Presenter {

    private var featureState: SendState = SendState.Static.Empty

    override fun attach(view: NewSendContract.View) {
        super.attach(view)
        sendTransactionDelegate.attach(this, view)
        stateMachine.observe()
            .onEach { state ->
                featureState = state
                uiRelay.handleFeatureState(state, view)
            }
            .launchIn(this)
    }

    override fun detach() {
        super.detach()
        sendTransactionDelegate.detach()
    }

    override fun updateToken(newToken: Token.Active) {
        stateMachine.newAction(SendFeatureAction.NewToken(SendToken.Common(newToken)))
    }

    override fun updateInputAmount(amount: String) {
        val bigDecimal = amount.toBigDecimal()
        stateMachine.newAction(
            if (bigDecimal.isZero()) {
                SendFeatureAction.ZeroAmount
            } else {
                SendFeatureAction.AmountChange(bigDecimal)
            }
        )
    }

    // di inject
    override fun setInitialData(selectedToken: Token.Active?, inputAmount: BigDecimal?) = Unit

    override fun switchCurrencyMode() {
        uiRelay.switchCurrencyMode()
    }

    override fun onMaxButtonClicked() {
        stateMachine.newAction(SendFeatureAction.MaxAmount)
    }

    override fun onTokenClicked() {
        launch {
            val tokens = userInteractor.getUserTokens()
            val result = tokens.filterNot(Token.Active::isZero)
            view?.showTokenSelection(tokens = result, selectedToken = requireToken())
        }
    }

    override fun onFeeInfoClicked() {
        view?.showFreeTransactionsInfo()
    }

    override fun send() {
        sendTransactionDelegate.send()
    }

    override fun checkInternetConnection() {
        if (!isInternetConnectionEnabled()) {
            view?.showUiKitSnackBar(
                message = resources.getString(R.string.error_no_internet_message),
                actionButtonResId = R.string.common_hide
            )
            view?.restoreSlider()
            return
        }

        view?.showSliderCompleteAnimation()
    }

    private fun isInternetConnectionEnabled(): Boolean =
        connectionStateProvider.hasConnection()

    private fun requireToken(): Token.Active =
        featureState.lastStaticState.tokenActive ?: error("Source token cannot be empty!")

    override fun updateFeePayerToken(feePayerToken: Token.Active) {
        // TODO("")
        /* try {
             sendInteractor.setFeePayerToken(feePayerToken)
             executeSmartSelection(
                 token = requireToken(),
                 feePayerToken = feePayerToken,
                 strategy = FeePayerSelectionStrategy.NO_ACTION
             )
         } catch (e: Throwable) {
             Timber.e(e, "Error updating fee payer token")
         }*/
    }
}
