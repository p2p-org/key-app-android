package org.p2p.wallet.history.ui.details

import android.content.Context
import org.p2p.wallet.R
import org.p2p.wallet.common.date.toDateTimeString
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.bottomsheet.DrawableContainer
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.Constants.REN_BTC_SYMBOL
import org.p2p.wallet.utils.SpanUtils
import org.p2p.wallet.utils.cutMiddle

class TransactionDetailsPresenter(
    private val transaction: HistoryTransaction,
    private val userLocalRepository: UserLocalRepository,
    private val context: Context
) : BasePresenter<TransactionDetailsContract.View>(),
    TransactionDetailsContract.Presenter {

    override fun attach(view: TransactionDetailsContract.View) {
        super.attach(view)
        when (transaction) {
            is HistoryTransaction.Swap -> parseSwap(transaction, view)
            is HistoryTransaction.Transfer -> parseTransfer(transaction, view)
            is HistoryTransaction.BurnOrMint -> parseBurnOrMint(transaction, view)
            else -> {
                // TODO: add support of other transactions
            }
        }
    }

    private fun parseSwap(transaction: HistoryTransaction.Swap, view: TransactionDetailsContract.View) {
        val title = transaction.getTitle()
        view.showTitle(title)
        view.showDate(transaction.date.toDateTimeString())

        view.showSourceInfo(
            iconContainer = DrawableContainer(transaction.sourceIconUrl),
            primaryInfo = transaction.getSourceTotal(),
            secondaryInfo = transaction.getSentUsdAmount()
        )

        view.showDestinationInfo(
            iconContainer = DrawableContainer(transaction.destinationIconUrl),
            primaryInfo = transaction.getDestinationTotal(),
            secondaryInfo = null
        )

        view.showSignature(transaction.signature)
        view.showAddresses(transaction.sourceAddress, transaction.destinationAddress)
        view.showAmount(R.string.details_amount, transaction.getFormattedAmount())
        view.showFee(null)
        view.showBlockNumber(transaction.getBlockNumber())
    }

    private fun parseTransfer(
        transaction: HistoryTransaction.Transfer,
        view: TransactionDetailsContract.View
    ) {
        val title = transaction.getTitle(context)
        view.showTitle(title)
        view.showDate(transaction.date.toDateTimeString())

        val tokenData = transaction.tokenData
        val isSend = transaction.isSend

        val iconRawContainer = DrawableContainer(tokenData.iconUrl.orEmpty())
        val iconResContainer = DrawableContainer(R.drawable.ic_wallet_gray)

        val formattedTotal = transaction.getFormattedTotal(scaleMedium = true)
        val formattedAmount = transaction.getFormattedAmount()

        view.showSourceInfo(
            iconContainer = if (isSend) iconRawContainer else iconResContainer,
            primaryInfo = if (isSend) formattedTotal else transaction.senderAddress.cutMiddle(),
            secondaryInfo = if (isSend) formattedAmount else null
        )
        view.showDestinationInfo(
            iconContainer = if (isSend) iconResContainer else iconRawContainer,
            primaryInfo = if (isSend) transaction.destination.cutMiddle() else formattedTotal,
            secondaryInfo = if (isSend) null else formattedAmount
        )
        view.showSignature(transaction.signature)
        view.showAddresses(transaction.senderAddress, transaction.destination)

        val usdTotal = "(${transaction.getFormattedAmount()})"
        val total = "${transaction.getFormattedTotal()} $usdTotal"
        val amount = SpanUtils.highlightText(total, usdTotal, context.getColor(R.color.textIconSecondary))
        view.showAmount(R.string.details_received, amount)
        view.showFee(null)
        view.showBlockNumber(transaction.getBlockNumber())
    }

    private fun parseBurnOrMint(
        transaction: HistoryTransaction.BurnOrMint,
        view: TransactionDetailsContract.View
    ) {
        val title = context.getString(transaction.getTitle())
        view.showTitle(title)
        view.showDate(transaction.date.toDateTimeString())

        val isBurn = transaction.isBurn

        val tokenData = userLocalRepository.findTokenDataBySymbol(REN_BTC_SYMBOL)
        val iconRawContainer = DrawableContainer(tokenData?.iconUrl.orEmpty())
        val iconResContainer = DrawableContainer(R.drawable.ic_wallet_gray)

        val formattedTotal = transaction.getFormattedTotal(scaleMedium = true)
        val formattedAmount = transaction.getFormattedAmount()

        view.showSourceInfo(
            iconContainer = if (isBurn) iconRawContainer else iconResContainer,
            primaryInfo = if (isBurn) formattedTotal else transaction.destination.cutMiddle(),
            secondaryInfo = if (isBurn) formattedAmount else null
        )
        view.showDestinationInfo(
            iconContainer = if (isBurn) iconResContainer else iconRawContainer,
            primaryInfo = if (isBurn) transaction.destination.cutMiddle() else formattedTotal,
            secondaryInfo = if (isBurn) null else formattedAmount
        )
        view.showSignature(transaction.signature)
        view.showAddresses(transaction.senderAddress, transaction.destination)

        val usdTotal = "(${transaction.getFormattedAmount()})"
        val total = "${transaction.getFormattedTotal()} $usdTotal"
        val amount = SpanUtils.highlightText(total, usdTotal, context.getColor(R.color.textIconSecondary))
        view.showAmount(R.string.details_received, amount)
        view.showFee(null)
        view.showBlockNumber(transaction.getBlockNumber())
    }
}