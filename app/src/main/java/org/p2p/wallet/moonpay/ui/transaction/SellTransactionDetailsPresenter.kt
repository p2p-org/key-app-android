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
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.unsafeLazy
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import java.util.Locale
import kotlinx.coroutines.launch

private const val DATE_FORMAT = "MMMM dd, yyyy"
private const val TIME_FORMAT = "HH:mm"

class SellTransactionDetailsPresenter(
    private val currentTransaction: SellTransactionViewDetails,
    private val sellInteractor: SellInteractor,
    private val userInteractor: UserInteractor,
    private val resources: Resources
) : BasePresenter<SellTransactionDetailsContract.View>(),
    SellTransactionDetailsContract.Presenter {

    private val dateFormat by unsafeLazy { DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.US) }
    private val timeFormat by unsafeLazy { DateTimeFormatter.ofPattern(TIME_FORMAT, Locale.US) }

    override fun attach(view: SellTransactionDetailsContract.View) {
        super.attach(view)

        val viewState = when (currentTransaction.status) {
            SellTransactionStatus.WAITING_FOR_DEPOSIT -> buildWaitingForDepositViewState()
            SellTransactionStatus.PENDING -> buildPendingViewState()
            SellTransactionStatus.COMPLETED -> buildCompletedViewState()
            SellTransactionStatus.FAILED -> buildFailedViewState()
        }
        view.renderViewState(viewState)
    }

    private fun buildWaitingForDepositViewState(): SellTransactionDetailsViewState {
        val titleBlock = SellTransactionDetailsViewState.TitleBlock(
            title = resources.getString(
                R.string.sell_details_waiting_deposit_title,
                currentTransaction.formattedSolAmount
            ),
            updatedAt = currentTransaction.updatedAtTitle(),
            boldAmount = resources.getString(
                R.string.sell_details_token_amount,
                currentTransaction.formattedSolAmount,
                Constants.SOL_SYMBOL
            ),
            labelAmount = null,
        )
        val bodyBlock = SellTransactionDetailsViewState.BodyBlock.rain(
            bodyText = resources.getString(R.string.sell_details_waiting_deposit_body),
        )
        val receiverBlock = SellTransactionDetailsViewState.ReceiverBlock(
            receiverTitle = resources.getString(R.string.sell_details_send_to),
            receiverValue = currentTransaction.receiverAddress.cutMiddle(),
            isCopyEnabled = true,
            copyValueProvider = currentTransaction::receiverAddress
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

    private fun buildPendingViewState(): SellTransactionDetailsViewState {
        val titleBlock = SellTransactionDetailsViewState.TitleBlock(
            title = resources.getString(
                R.string.sell_details_pending_title
            ),
            updatedAt = currentTransaction.updatedAtTitle(),
            boldAmount = resources.getString(
                R.string.sell_details_token_amount,
                currentTransaction.formattedSolAmount,
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

    private fun buildCompletedViewState(): SellTransactionDetailsViewState {
        val titleBlock = SellTransactionDetailsViewState.TitleBlock(
            title = resources.getString(
                R.string.sell_details_completed_title
            ),
            updatedAt = currentTransaction.updatedAtTitle(),
            boldAmount = resources.getString(
                R.string.sell_details_fiat_amount,
                currentTransaction.formattedFiatAmount,
                currentTransaction.fiatUiName
            ),
            labelAmount = resources.getString(
                R.string.sell_details_token_amount,
                currentTransaction.formattedSolAmount,
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

    private fun buildFailedViewState(): SellTransactionDetailsViewState {
        val titleBlock = SellTransactionDetailsViewState.TitleBlock(
            title = resources.getString(
                R.string.sell_details_failed_title,
                currentTransaction.formattedSolAmount
            ),
            updatedAt = currentTransaction.updatedAtTitle(),
            boldAmount = resources.getString(
                R.string.sell_details_token_amount,
                currentTransaction.formattedSolAmount,
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
            when (val result = sellInteractor.cancelTransaction(currentTransaction.transactionId)) {
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
            view?.navigateToSendScreen(
                tokenToSend = solToken,
                sendAmount = currentTransaction.formattedSolAmount.toBigDecimal(),
                receiverAddress = currentTransaction.receiverAddress
            )
        }
    }

    override fun onRemoveFromHistoryClicked() {
        sellInteractor.hideTransactionFromHistory(currentTransaction.transactionId)
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
