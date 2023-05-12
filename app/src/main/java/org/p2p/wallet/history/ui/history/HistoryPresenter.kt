package org.p2p.wallet.history.ui.history

import kotlinx.coroutines.launch
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.wallet.bridge.claim.ui.mapper.ClaimUiMapper
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.ifNotEmpty

class HistoryPresenter(
    private val userInteractor: UserInteractor,
    private val ethereumInteractor: EthereumInteractor,
    private val claimUiMapper: ClaimUiMapper,
    private val transactionManager: TransactionManager
) : BasePresenter<HistoryContract.View>(), HistoryContract.Presenter {

    override fun onBuyClicked() {
        launch {
            userInteractor.getTokensForBuy().ifNotEmpty {
                view?.showBuyScreen(it.first())
            }
        }
    }

    override fun onTransactionClicked(transactionId: String) {
        view?.openTransactionDetailsScreen(transactionId)
    }

    override fun onSellTransactionClicked(transactionId: String) {
        view?.openSellTransactionDetails(transactionId)
    }

    override fun onClaimPendingClicked(transactionId: String) {
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

    override fun onSendPendingClicked(transactionId: String) {
    }
}
