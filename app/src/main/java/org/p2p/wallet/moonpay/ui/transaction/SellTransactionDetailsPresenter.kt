package org.p2p.wallet.moonpay.ui.transaction

import android.content.res.Resources
import org.p2p.core.utils.Constants
import org.p2p.core.utils.getHtmlString
import org.p2p.core.utils.removeLinksUnderline
import org.p2p.wallet.R
import org.p2p.wallet.common.date.isSameDayAs
import org.p2p.wallet.common.date.toZonedDateTime
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellCancelResult
import org.p2p.wallet.moonpay.serversideapi.response.SellTransactionStatus
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.unsafeLazy
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import java.util.Locale
import kotlinx.coroutines.launch
import org.p2p.wallet.history.analytics.HistoryAnalytics
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.wallet.sell.interactor.HistoryItemMapper
import org.p2p.wallet.user.interactor.UserTokensInteractor
import org.p2p.wallet.utils.CUT_ADDRESS_SYMBOLS_COUNT

private const val DATE_FORMAT = "MMMM dd, yyyy"
private const val TIME_FORMAT = "HH:mm"

class SellTransactionDetailsPresenter(
    private val sellInteractor: SellInteractor,
    private val userInteractor: UserTokensInteractor,
    private val historyInteractor: HistoryInteractor,
    private val mapper: HistoryItemMapper,
    private val resources: Resources,
    private val historyAnalytics: HistoryAnalytics
) : BasePresenter<SellTransactionDetailsContract.View>(),
    SellTransactionDetailsContract.Presenter {

    private val dateFormat by unsafeLazy { DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.US) }
    private val timeFormat by unsafeLazy { DateTimeFormatter.ofPattern(TIME_FORMAT, Locale.US) }
    private var currentTransaction: SellTransactionViewDetails? = null

    override fun load(transactionId: String) {
        launch {
            try {
                currentTransaction = historyInteractor.findTransactionById(transactionId)
                    ?.takeIf { it is SellTransaction }
                    ?.let { mapper.toSellDetailsModel(it as SellTransaction) }

                val transaction = currentTransaction ?: return@launch
                val viewState = when (transaction.status) {
                    SellTransactionStatus.WAITING_FOR_DEPOSIT -> buildWaitingForDepositViewState(transaction)
                    SellTransactionStatus.PENDING -> buildPendingViewState(transaction)
                    SellTransactionStatus.COMPLETED -> buildCompletedViewState(transaction)
                    SellTransactionStatus.FAILED -> buildFailedViewState(transaction)
                }
                historyAnalytics.logSellTransactionClicked(transaction)
                view?.renderViewState(viewState)
            } catch (e: Throwable) {
                Timber.e(e, "Error on loading moonpay transaction details: $e")
                view?.showErrorMessage(e)
            }
        }
    }

    private fun buildWaitingForDepositViewState(
        transaction: SellTransactionViewDetails
    ): SellTransactionDetailsViewState {
        val titleBlock = SellTransactionDetailsViewState.TitleBlock(
            title = resources.getString(
                R.string.sell_details_waiting_deposit_title,
                transaction.formattedSolAmount
            ),
            updatedAt = transaction.updatedAtTitle(),
            boldAmount = resources.getString(
                R.string.sell_details_token_amount,
                transaction.formattedSolAmount,
                Constants.SOL_SYMBOL
            ),
            labelAmount = null,
        )
        val bodyBlock = SellTransactionDetailsViewState.BodyBlock.rain(
            bodyText = resources.getString(R.string.sell_details_waiting_deposit_body),
        )
        val receiverBlock = SellTransactionDetailsViewState.ReceiverBlock(
            receiverTitle = resources.getString(R.string.sell_details_send_to),
            receiverValue = transaction.receiverAddress.cutMiddle(cutCount = CUT_ADDRESS_SYMBOLS_COUNT),
            isCopyEnabled = true,
            copyValueProvider = transaction::receiverAddress
        )
        val buttonsBlock = SellTransactionDetailsViewState.ButtonsBlock(
            mainButtonTitle = resources.getString(R.string.sell_details_button_send),
            mainButtonAction = ::onSendClicked,
            additionalButtonTitle = resources.getString(R.string.sell_details_button_cancel),
            additionalButtonAction = ::onCancelTransactionClicked
        )
        return SellTransactionDetailsViewState(
            titleBlock = titleBlock,
            messageBlock = bodyBlock,
            receiverBlock = receiverBlock,
            buttonsBlock = buttonsBlock
        )
    }

    private fun buildPendingViewState(transaction: SellTransactionViewDetails): SellTransactionDetailsViewState {
        val titleBlock = SellTransactionDetailsViewState.TitleBlock(
            title = resources.getString(
                R.string.sell_details_pending_title
            ),
            updatedAt = transaction.updatedAtTitle(),
            boldAmount = resources.getString(
                R.string.sell_details_token_amount,
                transaction.formattedSolAmount,
                Constants.SOL_SYMBOL
            ),
            labelAmount = null,
        )
        val bodyBlock = SellTransactionDetailsViewState.BodyBlock.silver(
            bodyText = resources.getHtmlString(R.string.sell_details_pending_body).removeLinksUnderline(),
        )
        val receiverBlock = SellTransactionDetailsViewState.ReceiverBlock(
            receiverTitle = resources.getString(R.string.sell_details_will_be_send_to),
            receiverValue = resources.getString(R.string.sell_details_receiver_moonpay_bank),
            isCopyEnabled = false
        )
        val buttonsBlock = SellTransactionDetailsViewState.ButtonsBlock(
            mainButtonTitle = resources.getString(R.string.common_close),
            mainButtonAction = { view?.close() }
        )
        return SellTransactionDetailsViewState(
            titleBlock = titleBlock,
            messageBlock = bodyBlock,
            receiverBlock = receiverBlock,
            buttonsBlock = buttonsBlock
        )
    }

    private fun buildCompletedViewState(transaction: SellTransactionViewDetails): SellTransactionDetailsViewState {
        val titleBlock = SellTransactionDetailsViewState.TitleBlock(
            title = resources.getString(
                R.string.sell_details_completed_title
            ),
            updatedAt = transaction.updatedAtTitle(),
            boldAmount = resources.getString(
                R.string.sell_details_fiat_amount,
                transaction.formattedFiatAmount,
                transaction.fiatUiName
            ),
            labelAmount = resources.getString(
                R.string.sell_details_token_amount,
                transaction.formattedSolAmount,
                Constants.SOL_SYMBOL
            )
        )
        val bodyBlock = SellTransactionDetailsViewState.BodyBlock.silver(
            bodyText = resources.getHtmlString(R.string.sell_details_completed_body).removeLinksUnderline(),
        )
        val receiverBlock = SellTransactionDetailsViewState.ReceiverBlock(
            receiverTitle = resources.getString(R.string.sell_details_sent_to),
            receiverValue = resources.getString(R.string.sell_details_receiver_moonpay_bank),
            isCopyEnabled = false
        )
        val buttonsBlock = SellTransactionDetailsViewState.ButtonsBlock(
            mainButtonTitle = resources.getString(R.string.common_close),
            mainButtonAction = { view?.close() },
            additionalButtonTitle = resources.getString(R.string.sell_details_button_remove),
            additionalButtonAction = ::onRemoveFromHistoryClicked
        )
        return SellTransactionDetailsViewState(
            titleBlock = titleBlock,
            messageBlock = bodyBlock,
            receiverBlock = receiverBlock,
            buttonsBlock = buttonsBlock
        )
    }

    private fun buildFailedViewState(transaction: SellTransactionViewDetails): SellTransactionDetailsViewState {
        val titleBlock = SellTransactionDetailsViewState.TitleBlock(
            title = resources.getString(
                R.string.sell_details_failed_title,
                transaction.formattedSolAmount
            ),
            updatedAt = transaction.updatedAtTitle(),
            boldAmount = resources.getString(
                R.string.sell_details_token_amount,
                transaction.formattedSolAmount,
                Constants.SOL_SYMBOL
            ),
            labelAmount = null,
        )
        val bodyBlock = SellTransactionDetailsViewState.BodyBlock.rose(
            bodyText = resources.getString(R.string.sell_details_failed_body)
        )
        val buttonsBlock = SellTransactionDetailsViewState.ButtonsBlock(
            mainButtonTitle = resources.getString(R.string.sell_details_button_delete),
            mainButtonAction = ::onRemoveFromHistoryClicked
        )
        return SellTransactionDetailsViewState(
            titleBlock = titleBlock,
            messageBlock = bodyBlock,
            receiverBlock = null,
            buttonsBlock = buttonsBlock
        )
    }

    override fun onCancelTransactionClicked() {
        launch {
            val transaction = currentTransaction ?: return@launch
            when (val result = sellInteractor.cancelTransaction(transaction.transactionId)) {
                is MoonpaySellCancelResult.CancelSuccess -> {
                    view?.showUiKitSnackBar(messageResId = R.string.sell_details_cancel_success)
                    view?.close()
                }
                is MoonpaySellCancelResult.CancelFailed -> {
                    Timber.e(result.cause, "Failed to cancel transaction")
                    view?.showUiKitSnackBar(messageResId = R.string.sell_details_cancel_failed)
                }
            }
        }
    }

    override fun onSendClicked() {
        launch {
            val solToken = userInteractor.getUserSolToken() ?: return@launch
            val transaction = currentTransaction ?: return@launch
            view?.navigateToSendScreen(
                tokenToSend = solToken,
                sendAmount = transaction.formattedSolAmount.toBigDecimal(),
                receiverAddress = transaction.receiverAddress
            )
        }
    }

    override fun onRemoveFromHistoryClicked() {
        val transaction = currentTransaction ?: return
        sellInteractor.hideTransactionFromHistory(transaction.transactionId)
        view?.close()
    }

    private fun SellTransactionViewDetails.updatedAtTitle(): String {
        val time = updatedAt?.toZonedDateTime() ?: return emptyString()
        val now = ZonedDateTime.now()
        val isToday = time.isSameDayAs(now)
        // Today / August 21, 2022
        val datePart = if (isToday) resources.getString(R.string.common_today) else dateFormat.format(time)
        // 15:28
        val timePart = timeFormat.format(time)
        return resources.getString(R.string.transaction_date_format, datePart, timePart)
    }
}
