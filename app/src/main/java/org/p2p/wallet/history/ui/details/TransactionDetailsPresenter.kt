package org.p2p.wallet.history.ui.details

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.uikit.utils.SpanUtils
import org.p2p.wallet.R
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.date.toDateTimeString
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.core.common.DrawableContainer
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.TransactionDetailsLaunchState
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.core.utils.Constants.REN_BTC_SYMBOL
import org.p2p.wallet.utils.cutMiddle
import timber.log.Timber

private const val DELAY_IN_MS = 5000L

class TransactionDetailsPresenter(
    private val resourcesProvider: ResourcesProvider,
    private val state: TransactionDetailsLaunchState,
    private val userLocalRepository: UserLocalRepository,
    private val historyInteractor: HistoryInteractor
) : BasePresenter<TransactionDetailsContract.View>(),
    TransactionDetailsContract.Presenter {

    override fun attach(view: TransactionDetailsContract.View) {
        super.attach(view)
        load()
    }

    override fun load() {
        when (state) {
            is TransactionDetailsLaunchState.History -> handleHistory(state.transaction)
            is TransactionDetailsLaunchState.Id -> handleId(state)
        }
    }

    private fun handleId(state: TransactionDetailsLaunchState.Id) {
        launch {
            try {
                view?.showLoading(true)
                /* The new transaction is not being committed in blockchain yet, it requires some time to add our transaction,
                 * therefore we are giving some time to make our request not fail
                 * */
                delay(DELAY_IN_MS)
                val details = historyInteractor.getHistoryTransaction(state.tokenPublicKey)
                handleHistory(details)
            } catch (e: Throwable) {
                Timber.e(e, "Error loading transaction details")
                view?.showError(R.string.details_transaction_not_found)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private fun handleHistory(transaction: HistoryTransaction) {
        when (transaction) {
            is HistoryTransaction.Swap -> parseSwap(transaction)
            is HistoryTransaction.Transfer -> parseTransfer(transaction)
            is HistoryTransaction.BurnOrMint -> parseBurnOrMint(transaction)
            else -> {
                // TODO: add support of other transactions
            }
        }
    }

    private fun parseSwap(transaction: HistoryTransaction.Swap) {
        val title = transaction.getTitle()
        view?.showTitle(title)
        view?.showDate(transaction.date.toDateTimeString())
        view?.showStatus(transaction.status)

        view?.showSourceInfo(
            iconContainer = DrawableContainer(transaction.sourceIconUrl),
            primaryInfo = transaction.getSourceTotal(),
            secondaryInfo = transaction.getSentUsdAmount()
        )

        view?.showDestinationInfo(
            iconContainer = DrawableContainer(transaction.destinationIconUrl),
            primaryInfo = transaction.getDestinationTotal(),
            secondaryInfo = null
        )

        view?.showSignature(transaction.signature)
        view?.showAddresses(transaction.sourceAddress, transaction.destinationAddress)
        view?.showAmount(R.string.details_amount, transaction.getFormattedAmount())
        view?.showFee(null)
        view?.showBlockNumber(transaction.getBlockNumber())
    }

    private fun parseTransfer(transaction: HistoryTransaction.Transfer) {
        val title = transaction.getTitle(resourcesProvider.resources)
        view?.showTitle(title)
        view?.showDate(transaction.date.toDateTimeString())
        view?.showStatus(transaction.status)

        val tokenData = transaction.tokenData
        val isSend = transaction.isSend

        val iconRawContainer = DrawableContainer(tokenData.iconUrl.orEmpty())
        val iconResContainer = DrawableContainer(R.drawable.ic_wallet_gray)

        val formattedTotal = transaction.getFormattedTotal(scaleMedium = true)
        val formattedAmount = transaction.getFormattedAmount()

        view?.showSourceInfo(
            iconContainer = if (isSend) iconRawContainer else iconResContainer,
            primaryInfo = if (isSend) formattedTotal else transaction.senderAddress.cutMiddle(),
            secondaryInfo = if (isSend) formattedAmount else null
        )
        view?.showDestinationInfo(
            iconContainer = if (isSend) iconResContainer else iconRawContainer,
            primaryInfo = if (isSend) transaction.destination.cutMiddle() else formattedTotal,
            secondaryInfo = if (isSend) null else formattedAmount
        )
        view?.showSignature(transaction.signature)
        view?.showAddresses(transaction.senderAddress, transaction.destination)

        val usdTotal = "(${transaction.getFormattedAmount()})"
        val total = "${transaction.getFormattedTotal()} $usdTotal"
        val amount = SpanUtils.highlightText(
            commonText = total,
            highlightedText = usdTotal,
            color = resourcesProvider.getColor(R.color.textIconSecondary)
        )
        view?.showAmount(R.string.details_received, amount)
        view?.showFee(null)
        view?.showBlockNumber(transaction.getBlockNumber())
    }

    private fun parseBurnOrMint(transaction: HistoryTransaction.BurnOrMint) {
        val title = resourcesProvider.getString(transaction.getTitle())
        view?.showTitle(title)
        view?.showDate(transaction.date.toDateTimeString())

        val isBurn = transaction.isBurn

        val tokenData = userLocalRepository.findTokenDataBySymbol(REN_BTC_SYMBOL)
        val iconRawContainer = DrawableContainer(tokenData?.iconUrl.orEmpty())
        val iconResContainer = DrawableContainer(R.drawable.ic_wallet_gray)

        val formattedTotal = transaction.getFormattedTotal(scaleMedium = true)
        val formattedAmount = transaction.getFormattedAmount()

        view?.showSourceInfo(
            iconContainer = if (isBurn) iconRawContainer else iconResContainer,
            primaryInfo = if (isBurn) formattedTotal else transaction.destination.cutMiddle(),
            secondaryInfo = if (isBurn) formattedAmount else null
        )
        view?.showDestinationInfo(
            iconContainer = if (isBurn) iconResContainer else iconRawContainer,
            primaryInfo = if (isBurn) transaction.destination.cutMiddle() else formattedTotal,
            secondaryInfo = if (isBurn) null else formattedAmount
        )
        view?.showSignature(transaction.signature)
        view?.showAddresses(transaction.senderAddress, transaction.destination)

        val usdTotal = "(${transaction.getFormattedAmount()})"
        val total = "${transaction.getFormattedTotal()} $usdTotal"
        val amount = SpanUtils.highlightText(
            commonText = total,
            highlightedText = usdTotal,
            color = resourcesProvider.getColor(R.color.textIconSecondary)
        )
        view?.showAmount(R.string.details_received, amount)
        view?.showFee(null)
        view?.showBlockNumber(transaction.getBlockNumber())
    }
}
