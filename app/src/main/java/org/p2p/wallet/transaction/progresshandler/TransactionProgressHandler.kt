package org.p2p.wallet.transaction.progresshandler

import androidx.annotation.CallSuper
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import org.p2p.core.glide.GlideManager
import org.p2p.uikit.utils.SpanUtils
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.getString
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.databinding.DialogNewTransactionProgressBinding
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.transaction.model.progressstate.TransactionState
import org.p2p.wallet.utils.unsafeLazy

private const val IMAGE_SIZE = 64
private const val DATE_FORMAT = "MMMM dd, yyyy"
private const val TIME_FORMAT = "HH:mm"

abstract class TransactionProgressHandler(private val glideManager: GlideManager) {

    private val dateFormat by unsafeLazy { DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.US) }
    private val timeFormat by unsafeLazy { DateTimeFormatter.ofPattern(TIME_FORMAT, Locale.US) }

    protected lateinit var binding: DialogNewTransactionProgressBinding
    protected lateinit var progressStateFormat: String

    fun init(viewBinding: DialogNewTransactionProgressBinding, data: NewShowProgress) {
        binding = viewBinding
        progressStateFormat = viewBinding.getString(R.string.transaction_progress_title)
        handleInitState(data)
    }

    open fun handleInitState(data: NewShowProgress) {
        with(binding) {
            val colorMountain = getColor(R.color.text_mountain)
            textViewSubtitle.text = getString(
                R.string.transaction_date_format,
                dateFormat.format(data.date),
                timeFormat.format(data.date)
            )
            glideManager.load(imageViewToken, data.tokenUrl, IMAGE_SIZE)
            val amountInUsd = data.amountUsd
            if (amountInUsd != null) {
                textViewAmountUsd.text = data.amountUsd
                textViewAmountTokens.text = data.amountTokens
            } else {
                textViewAmountUsd.text = data.amountTokens
                textViewAmountTokens.isVisible = false
            }
            data.amountColor?.let { amountColorRes ->
                textViewAmountUsd.setTextColorRes(amountColorRes)
            }
            if (data.recipient == null) {
                textViewSendToTitle.isVisible = false
                textViewSendToValue.isVisible = false
            } else {
                textViewSendToValue.text = data.recipient
            }
            val totalFees = data.totalFees
            textViewFeeValue.text = if (totalFees != null) {
                buildSpannedString {
                    totalFees.forEach { textToHighlight ->
                        append(
                            SpanUtils.highlightText(
                                commonText = textToHighlight.commonText,
                                highlightedText = textToHighlight.highlightedText,
                                color = colorMountain
                            )
                        )
                        append("\n")
                    }
                }
            } else {
                getString(R.string.transaction_transaction_fee_free_value)
            }
        }
    }

    /*
    * it is better to call super for handling TransactionState.Progress on start
    * and not to duplicate code
    **/
    @CallSuper
    open fun handleState(state: TransactionState) {
        when (state) {
            is TransactionState.Progress -> setProgressState(state)
            is TransactionState.Success -> setSuccessState()
            is TransactionState.Error -> setErrorState()
            else -> Unit
        }
    }

    protected fun setProgressState(state: TransactionState.Progress) {
        with(binding) {
            textViewTitle.text = progressStateFormat.format(getString(R.string.transaction_progress_submitted))
            progressStateTransaction.setDescriptionText(state.description)
            buttonDone.setText(R.string.common_done)
        }
    }

    protected fun setSuccessState() {
        with(binding) {
            textViewTitle.text = progressStateFormat.format(getString(R.string.transaction_progress_succeeded))
            progressStateTransaction.setSuccessState()
            progressStateTransaction.setDescriptionText(R.string.transaction_description_succeeded)
            buttonDone.setText(R.string.common_done)
        }
    }

    protected fun setErrorState() {
        with(binding) {
            textViewTitle.text = progressStateFormat.format(getString(R.string.transaction_progress_failed))
            textViewAmountUsd.setTextColorRes(R.color.text_rose)
            progressStateTransaction.setFailedState()
            progressStateTransaction.setDescriptionText(R.string.transaction_description_general_failed)
            buttonDone.setText(R.string.common_close)
        }
    }
}
