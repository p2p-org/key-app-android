package org.p2p.wallet.history.ui.token

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.wallet.R
import org.p2p.wallet.bridge.claim.ui.mapper.ClaimUiMapper
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.rpc.interactor.TokenInteractor
import org.p2p.wallet.transaction.model.TransactionState

class TokenHistoryPresenter(
    private val token: Token.Active,
    private val tokenInteractor: TokenInteractor,
    private val ethAddressEnabledFeatureToggle: EthAddressEnabledFeatureToggle,
    private val ethereumInteractor: EthereumInteractor,
    private val transactionManager: TransactionManager,
    private val claimUiMapper: ClaimUiMapper
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

    override fun onBridgePendingClaimClicked(transactionId: String) {
        launch {
            val bridgeBundle = ethereumInteractor.getBundleById(transactionId) ?: return@launch
            val claimDetails = claimUiMapper.makeClaimDetails(
                resultAmount = bridgeBundle.resultAmount,
                fees = bridgeBundle.fees,
                isFree = bridgeBundle.compensationDeclineReason.isEmpty(),
                minAmountForFreeFee = ethereumInteractor.getClaimMinAmountForFreeFee(),
                transactionDate = bridgeBundle.dateCreated
            )
            val amountToClaim = bridgeBundle.resultAmount.amountInToken
            val iconUrl =
                ERC20Tokens.values().firstOrNull { it.contractAddress == bridgeBundle.findToken().hex }?.tokenIconUrl
            val progressDetails = claimUiMapper.prepareShowProgress(
                amountToClaim = amountToClaim,
                iconUrl = iconUrl.orEmpty(),
                claimDetails = claimDetails
            )
            transactionManager.emitTransactionState(
                transactionId,
                TransactionState.ClaimProgress(transactionId)
            )
            view?.showProgressDialog(
                bundleId = bridgeBundle.bundleId,
                progressDetails = progressDetails
            )
        }
    }

    override fun onBridgePendingSendClicked(transactionId: String) {
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

    override fun onReceiveClicked() {
        if (ethAddressEnabledFeatureToggle.isFeatureEnabled) {
            if (token.mintAddress in ERC20Tokens.values().map { it.receiveFromTokens }.flatten()) {
                view?.showReceiveNetworkDialog()
            } else {
                view?.openReceiveInSolana()
            }
        } else {
            view?.openOldReceiveInSolana()
        }
    }
}
