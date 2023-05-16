package org.p2p.wallet.history.ui.history

import kotlinx.coroutines.launch
import org.p2p.core.utils.asNegativeUsdTransaction
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.wallet.R
import org.p2p.wallet.bridge.claim.ui.mapper.ClaimUiMapper
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.bridge.send.ui.mapper.BridgeSendUiMapper
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.ifNotEmpty

class HistoryPresenter(
    private val userInteractor: UserInteractor,
    private val ethereumInteractor: EthereumInteractor,
    private val claimUiMapper: ClaimUiMapper,
    private val bridgeSendUiMapper: BridgeSendUiMapper,
    private val transactionManager: TransactionManager,
    private val userRepository: UserLocalRepository
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
            val bridgeBundle = ethereumInteractor.getClaimBundleById(transactionId) ?: return@launch
            val claimDetails = claimUiMapper.makeClaimDetails(
                resultAmount = bridgeBundle.resultAmount,
                fees = bridgeBundle.fees,
                isFree = bridgeBundle.compensationDeclineReason.isEmpty(),
                minAmountForFreeFee = ethereumInteractor.getClaimMinAmountForFreeFee(),
                transactionDate = bridgeBundle.dateCreated
            )
            val amountToClaim = bridgeBundle.resultAmount.amountInToken
            val iconUrl = ERC20Tokens.findToken(bridgeBundle.findTokenOrDefaultEth()).tokenIconUrl
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
        launch {
            val sendBundle = ethereumInteractor.getSendBundleById(transactionId) ?: return@launch
            val token = userRepository.getTokensData().firstOrNull { it.symbol == sendBundle.amount.symbol }
            val feeDetails = bridgeSendUiMapper.makeBridgeFeeDetails(
                recipientAddress = sendBundle.recipient.raw,
                fees = sendBundle.fees
            )
            val progressDetails = bridgeSendUiMapper.prepareShowProgress(
                iconUrl = token?.iconUrl.orEmpty(),
                amountTokens = "${sendBundle.amount.amountInToken.toPlainString()} ${token?.symbol}",
                amountUsd = sendBundle.amount.amountInUsd.toBigDecimalOrZero().asNegativeUsdTransaction(),
                recipient = sendBundle.recipient.raw,
                feeDetails = feeDetails
            )
            val progressState = TransactionState.Progress(
                description = R.string.bridge_send_transaction_description_progress
            )

            transactionManager.emitTransactionState(transactionId, progressState)
            view?.showProgressDialog(transactionId, progressDetails)
        }
    }
}
