package org.p2p.wallet.transaction.progresshandler

import androidx.annotation.CallSuper
import androidx.annotation.ColorRes
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Context
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import org.p2p.core.common.TextContainer
import org.p2p.core.glide.GlideManager
import org.p2p.core.model.TextHighlighting
import org.p2p.core.model.TitleValue
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.finance_block.mainCellDelegate
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.utils.SpanUtils
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.context
import org.p2p.uikit.utils.getString
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.databinding.DialogNewTransactionProgressBinding
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.transaction.model.progressstate.TransactionState
import org.p2p.wallet.utils.unsafeLazy

private const val IMAGE_SIZE = 64
private const val DATE_FORMAT = "MMMM dd, yyyy"
private const val TIME_FORMAT = "HH:mm"

/**
 * Base class for rendering and representing transaction details bottom sheet
 */
abstract class TransactionProgressHandler(private val glideManager: GlideManager) {

    private val dateFormat by unsafeLazy { DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.US) }
    private val timeFormat by unsafeLazy { DateTimeFormatter.ofPattern(TIME_FORMAT, Locale.US) }

    protected lateinit var binding: DialogNewTransactionProgressBinding
    protected lateinit var progressStateFormat: String

    val context: Context
        get() = binding.context

    private val transactionDetailsAdapter = CommonAnyCellAdapter(
        mainCellDelegate()
    )

    /**
     * init this handler with binding from the bottom sheet
     * called when the bottom sheet is created
     */
    fun init(viewBinding: DialogNewTransactionProgressBinding, data: NewShowProgress) {
        binding = viewBinding
        progressStateFormat = context.getString(R.string.transaction_progress_title)

        handleInitState(data)
    }

    /**
     * override this if you want to fill all the info about transaction by hand
     *
     * base class has default implementation that fills subtitle, loads token icon, sets amounts
     * and renders custom transaction details list
     */
    open fun handleInitState(showProgressData: NewShowProgress) {
        renderSubtitle(showProgressData.date)
        renderTokenIcon(showProgressData.tokenUrl)
        renderAmounts(
            amountTokens = showProgressData.amountTokens,
            amountInUsd = showProgressData.amountUsd,
            amountColorRes = showProgressData.amountColor
        )
        renderTransactionDetailsList(
            totalFees = showProgressData.totalFees,
            customTransactionDetails = showProgressData.transactionDetails
        )
    }

    private fun renderSubtitle(date: ZonedDateTime) {
        binding.textViewSubtitle.text = context.getString(
            R.string.transaction_date_format,
            dateFormat.format(date),
            timeFormat.format(date)
        )
    }

    private fun renderTokenIcon(tokenUrl: String) {
        glideManager.load(
            imageView = binding.imageViewToken,
            url = tokenUrl,
            size = IMAGE_SIZE
        )
    }

    private fun renderAmounts(
        amountTokens: String,
        amountInUsd: String?,
        @ColorRes amountColorRes: Int?
    ) {
        with(binding) {
            if (amountInUsd != null) {
                textViewAmountUsd.text = amountInUsd
                textViewAmountTokens.text = amountTokens
            } else {
                textViewAmountUsd.text = amountTokens
                textViewAmountTokens.isVisible = false
            }
            if (amountColorRes != null) {
                textViewAmountUsd.setTextColorRes(amountColorRes)
            }
        }
    }

    private fun renderTransactionDetailsList(
        totalFees: List<TextHighlighting>?,
        customTransactionDetails: List<TitleValue>
    ) {
        binding.recyclerViewTransactionData.attachAdapter(transactionDetailsAdapter)
        binding.recyclerViewTransactionData.layoutManager = LinearLayoutManager(context)

        val feesCellModel = TitleValue(
            title = context.getString(R.string.transaction_transaction_fee_title),
            value = getTotalFeesValue(totalFees).toString()
        ).toMainCellModel()

        val customTransactionDetails = customTransactionDetails.map { it.toMainCellModel() }
        transactionDetailsAdapter.items = customTransactionDetails + feesCellModel
    }

    private fun getTotalFeesValue(totalFees: List<TextHighlighting>?): CharSequence {
        if (totalFees == null) {
            return context.getString(R.string.transaction_transaction_fee_free_value)
        }

        val colorMountain = context.getColor(R.color.text_mountain)
        return buildSpannedString {
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
    }

    private fun TitleValue.toMainCellModel(): MainCellModel {
        val leftSideText = LeftSideCellModel.IconWithText(
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(title),
                textAppearance = R.style.UiKit_TextAppearance_Regular_Text4,
                textColor = R.color.text_mountain
            ),
        )
        val rightSideText = RightSideCellModel.TwoLineText(
            firstLineText = TextViewCellModel.Raw(TextContainer(value)),
        )
        return MainCellModel(
            leftSideCellModel = leftSideText,
            rightSideCellModel = rightSideText
        )
    }

    /**
     * Bottom sheet calls this method if a new transaction state is emitted from [TransactionManager]
     * override this method if you want to fill your own text / string when reacting to transaction statuses]
     *
     * @CallSuper for handling TransactionState.Progress on start and not to duplicate code
     */
    @CallSuper
    open fun handleState(state: TransactionState) {
        when (state) {
            is TransactionState.Progress -> setProgressState(state)
            is TransactionState.Success -> setSuccessState()
            is TransactionState.Error -> setErrorState()
            else -> Unit
        }
    }

    protected fun setProgressState(state: TransactionState.Progress) = with(binding) {
        textViewTitle.text = progressStateFormat.format(getString(R.string.transaction_progress_submitted))
        progressStateTransaction.setDescriptionText(state.description)
        buttonDone.setText(R.string.common_done)
    }

    protected fun setSuccessState() = with(binding) {
        textViewTitle.text = progressStateFormat.format(getString(R.string.transaction_progress_succeeded))
        progressStateTransaction.setSuccessState()
        progressStateTransaction.setDescriptionText(R.string.transaction_description_succeeded)
        buttonDone.setText(R.string.common_done)
    }

    protected fun setErrorState() = with(binding) {
        textViewTitle.text = progressStateFormat.format(getString(R.string.transaction_progress_failed))
        textViewAmountUsd.setTextColorRes(R.color.text_rose)
        progressStateTransaction.setFailedState()
        progressStateTransaction.setDescriptionText(R.string.transaction_description_general_failed)
        buttonDone.setText(R.string.common_close)
    }
}
