package org.p2p.wallet.history.ui.token

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.wallet.R
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.rpc.interactor.TokenInteractor

class TokenHistoryPresenter(
    private val token: Token.Active,
    private val tokenInteractor: TokenInteractor,
    private val ethAddressEnabledFeatureToggle: EthAddressEnabledFeatureToggle
) : BasePresenter<TokenHistoryContract.View>(), TokenHistoryContract.Presenter {

    override fun attach(view: TokenHistoryContract.View) {
        super.attach(view)
        initialize()
    }

    private fun initialize() {
        val actionButtons = mutableListOf(
            ActionButton.RECEIVE_BUTTON,
            ActionButton.SEND_BUTTON,
            ActionButton.SWAP_BUTTON
        )

        if (token.isSOL || token.isUSDC) {
            actionButtons.add(0, ActionButton.BUY_BUTTON)
        }

        view?.showActionButtons(actionButtons)
    }

    override fun onTransactionClicked(transactionId: String) {
        view?.showDetailsScreen(transactionId)
    }

    override fun onSellTransactionClicked(transactionId: String) {
        view?.openSellTransactionDetails(transactionId)
    }

    override fun onReceiveClicked() {
        if (ethAddressEnabledFeatureToggle.isFeatureEnabled) {
            view?.showReceiveTokensScreen()
        } else {
            view?.showReceiveTokenScreen()
        }
    }

    override fun closeAccount() {
        launch {
            try {
                tokenInteractor.closeTokenAccount(token.publicKey)
                view?.showUiKitSnackBar(messageResId = R.string.details_account_closed_successfully)
            } catch (e: Throwable) {
                Timber.e(e, "Error closing account: ${token.publicKey}")
                view?.showErrorMessage(e)
            }
        }
    }
}
