package org.p2p.wallet.history.ui.detailsbottomsheet

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import org.p2p.core.crypto.Base58String
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.history.model.rpc.RpcFee

interface HistoryTransactionDetailsContract {

    interface View : MvpView {
        fun showTransferView(tokenIconUrl: String?, placeholderIcon: Int)
        fun setSmokeBackground()
        fun showNewButtons(
            @StringRes firstButtonTitleRes: Int,
            @StringRes secondButtonTitleRes: Int,
        )
        fun playApplauseAnimation()
        fun showSwapView(sourceIconUrl: String?, destinationIconUrl: String?)
        fun showSubtitle(subtitle: String)
        fun showStatus(@StringRes titleResId: Int, @ColorRes colorId: Int)
        fun setTitle(@StringRes titleResId: Int)
        fun showProgressTransactionErrorState(errorMessage: String)
        fun showTransactionId(signature: String)
        fun showAmount(amountToken: String?, amountUsd: String?)
        fun showAmountReferralReward(amountToken: String)
        fun showFee(fees: List<RpcFee>?)
        fun showLoading(isLoading: Boolean)
        fun showError(@StringRes messageId: Int)
        fun showReceiverAddress(receiverAddress: Base58String, receiverUsername: String?, toEth: Boolean)
        fun showStateTitleValue(title: String, value: String)
        fun hideSendReceiveTitleAndValue()
        fun showSenderAddress(senderAddress: Base58String, senderUsername: String?, isReceivePending: Boolean)
        fun showProgressTransactionInProgress()
        fun dismiss()
        fun navigateToSolscan(txSignature: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun load(transactionId: String)
        fun onPrimaryButtonClick()
        fun onSecondaryButtonClick()
    }
}
