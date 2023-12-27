package org.p2p.wallet.history.ui.detailsbottomsheet

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.text.buildSpannedString
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.p2p.core.glide.GlideManager
import org.p2p.core.utils.DEFAULT_DECIMAL
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.lessThenMinValue
import org.p2p.core.utils.orZero
import org.p2p.uikit.utils.SpanUtils
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogHistoryTransactionDetailsBinding
import org.p2p.wallet.history.model.rpc.RpcFee
import org.p2p.core.crypto.Base58String
import org.p2p.wallet.utils.CUT_7_SYMBOLS
import org.p2p.wallet.utils.UsernameFormatter
import org.p2p.wallet.utils.appendWhitespace
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.shareText
import org.p2p.wallet.utils.showInfoDialog
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrGone

private const val EXTRA_TRANSACTION_ID = "EXTRA_TRANSACTION_ID"

private const val IMAGE_SIZE = 64

class HistoryTransactionDetailsBottomSheetFragment :
    BaseMvpBottomSheet<HistoryTransactionDetailsContract.View, HistoryTransactionDetailsContract.Presenter>(
        R.layout.dialog_history_transaction_details
    ),
    HistoryTransactionDetailsContract.View {

    companion object {
        fun show(fragmentManager: FragmentManager, transactionId: String) {
            HistoryTransactionDetailsBottomSheetFragment()
                .withArgs(EXTRA_TRANSACTION_ID to transactionId)
                .show(fragmentManager, HistoryTransactionDetailsBottomSheetFragment::javaClass.name)
        }

        fun hide(fragmentManager: FragmentManager) {
            val dialog = fragmentManager.findFragmentByTag(HistoryTransactionDetailsBottomSheetFragment::javaClass.name)
            (dialog as? HistoryTransactionDetailsBottomSheetFragment)?.dismissAllowingStateLoss()
        }
    }

    private val transactionId: String by args(EXTRA_TRANSACTION_ID)

    private val binding: DialogHistoryTransactionDetailsBinding by viewBinding()

    private val glideManager: GlideManager by inject()

    private val usernameFormatter: UsernameFormatter by inject()

    override val presenter: HistoryTransactionDetailsContract.Presenter by inject()

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow

    private val titleStateFormat: String by unsafeLazy { getString(R.string.transaction_details_title) }

    private val lessThenMinString by unsafeLazy { getString(R.string.common_less_than_minimum_with_dollar) }

    override fun onStart() {
        super.onStart()
        BottomSheetBehavior.from(requireView().parent as View).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.load(transactionId)
    }

    override fun showError(@StringRes messageId: Int) {
        showInfoDialog(
            titleRes = R.string.error_general_title,
            messageRes = messageId,
            primaryButtonRes = R.string.common_retry,
            primaryCallback = { presenter.load(transactionId) },
            secondaryButtonRes = R.string.common_ok,
            secondaryCallback = { dismissAllowingStateLoss() },
            isCancelable = false
        )
    }

    override fun showTransferView(tokenIconUrl: String?, placeholderIcon: Int) = with(binding) {
        imageViewSecondToken.isVisible = false
        if (tokenIconUrl.isNullOrEmpty()) {
            imageViewFirstToken.scaleType = ImageView.ScaleType.CENTER
            imageViewFirstToken.setImageResource(placeholderIcon)
        } else {
            imageViewFirstToken.scaleType = ImageView.ScaleType.FIT_CENTER
            glideManager.load(
                imageView = imageViewFirstToken,
                url = tokenIconUrl,
                size = IMAGE_SIZE,
                placeholder = placeholderIcon
            )
        }
    }

    override fun showSwapView(sourceIconUrl: String?, destinationIconUrl: String?) = with(binding) {
        glideManager.apply {
            load(imageViewFirstToken, sourceIconUrl, IMAGE_SIZE)
            load(imageViewSecondToken, destinationIconUrl, IMAGE_SIZE)
        }
        imageViewSecondToken.isVisible = true
    }

    override fun showSubtitle(subtitle: String) {
        binding.textViewSubtitle.text = subtitle
    }

    override fun showStatus(@StringRes titleResId: Int, @ColorRes colorId: Int) = with(binding) {
        textViewTitle.text = titleStateFormat.format(getString(titleResId))
        textViewAmountUsd.setTextColor(getColor(colorId))
    }

    override fun showProgressTransactionErrorState(errorMessage: String) = with(binding.progressStateTransaction) {
        isVisible = true
        setFailedState()
        setDescriptionText(errorMessage)
    }

    override fun showProgressTransactionInProgress() = with(binding.progressStateTransaction) {
        isVisible = true
        setDescriptionText(R.string.transaction_details_pending_transfer_description)
    }

    override fun showTransactionId(signature: String) {
        with(binding) {
            val url = getString(R.string.solanaExplorer, signature)
            imageViewShare.setOnClickListener {
                requireContext().shareText(url)
            }
            imageViewExplorer.setOnClickListener {
                showUrlInCustomTabs(url)
            }
        }
    }

    override fun showSenderAddress(
        senderAddress: Base58String,
        senderUsername: String?,
        isReceivePending: Boolean
    ) = with(binding) {
        textViewSendReceiveValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_copy_filled_16, 0)
        textViewSendReceiveTitle.text = if (isReceivePending) {
            getString(R.string.transaction_details_from)
        } else {
            getString(R.string.transaction_details_receive_from)
        }
        if (senderUsername != null) {
            textViewSendReceiveValue.text = usernameFormatter.format(senderUsername)
            textViewSendReceiveValue.setOnClickListener {
                requireContext().copyToClipBoard(senderUsername)
                showUiKitSnackBar(
                    messageResId = R.string.transaction_details_sender_username_copied,
                    actionButtonResId = R.string.common_hide,
                    actionBlock = Snackbar::dismiss
                )
            }
        } else {
            textViewSendReceiveValue.text = senderAddress.base58Value.cutMiddle(CUT_7_SYMBOLS)
            textViewSendReceiveValue.setOnClickListener {
                requireContext().copyToClipBoard(senderAddress.base58Value)
                showUiKitSnackBar(
                    messageResId = R.string.transaction_details_sender_address_copied,
                    actionButtonResId = R.string.common_hide,
                    actionBlock = Snackbar::dismiss
                )
            }
        }
    }

    override fun showReceiverAddress(
        receiverAddress: Base58String,
        receiverUsername: String?,
        toEth: Boolean
    ) = with(binding) {
        textViewSendReceiveValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_copy_filled_16, 0)
        textViewSendReceiveTitle.text = getString(
            if (toEth) {
                R.string.bridge_details_send
            } else {
                R.string.transaction_details_send_to
            }
        )
        if (receiverUsername != null) {
            textViewSendReceiveValue.text = usernameFormatter.format(receiverUsername)
            textViewSendReceiveValue.setOnClickListener {
                requireContext().copyToClipBoard(receiverUsername)
                showUiKitSnackBar(
                    messageResId = R.string.transaction_details_receiver_username_copied,
                    actionButtonResId = R.string.common_hide,
                    actionBlock = Snackbar::dismiss
                )
            }
        } else {
            textViewSendReceiveValue.text = receiverAddress.base58Value.cutMiddle(CUT_7_SYMBOLS)
            textViewSendReceiveValue.setOnClickListener {
                requireContext().copyToClipBoard(receiverAddress.base58Value)
                showUiKitSnackBar(
                    messageResId = R.string.transaction_details_receiver_address_copied,
                    actionButtonResId = R.string.common_hide,
                    actionBlock = Snackbar::dismiss
                )
            }
        }
    }

    override fun showStateTitleValue(title: String, value: String) = with(binding) {
        textViewSendReceiveTitle.text = title
        textViewSendReceiveValue.text = value
    }

    override fun hideSendReceiveTitleAndValue() = with(binding) {
        textViewSendReceiveTitle.isGone = true
        textViewSendReceiveValue.isGone = true
    }

    override fun showAmount(amountToken: String?, amountUsd: String?) = with(binding) {
        textViewAmountTokens.withTextOrGone(amountToken)
        textViewAmountUsd.withTextOrGone(amountUsd)
    }

    override fun showFee(fees: List<RpcFee>?) = with(binding) {
        textViewFeeTitle.isVisible = true
        textViewFeeValue.isVisible = true
        textViewFeeValue.text = fees.formatFees(lessThenMinString)
    }

    override fun showLoading(isLoading: Boolean) {
        binding.layoutContent.isGone = isLoading
        binding.viewProgress.isVisible = isLoading
    }

    override fun onStop() {
        dismissAllowingStateLoss()
        super.onStop()
    }

    private fun List<RpcFee>?.formatFees(lessThenMinString: String): CharSequence {
        return if (this.isNullOrEmpty()) {
            getString(R.string.transaction_transaction_fee_free_value)
        } else {
            val fees = this
            buildSpannedString {
                fees.mapIndexed { index, feeAmount ->
                    val isNotLast = index != size - 1
                    val feeInFiat = feeAmount.totalInUsd.orZero()
                    val formattedUsdAmount = if (feeInFiat.lessThenMinValue()) {
                        lessThenMinString
                    } else {
                        "$${feeInFiat.formatFiat()}"
                    }

                    val highlightedText = "($formattedUsdAmount)"
                    val commonText = buildString {
                        append(feeAmount.totalInTokens.formatToken(feeAmount.tokensDecimals ?: DEFAULT_DECIMAL))
                        appendWhitespace()
                        append(feeAmount.tokenSymbol.orEmpty())
                        appendWhitespace()
                        append(highlightedText)
                    }

                    append(
                        SpanUtils.highlightText(
                            commonText,
                            highlightedText,
                            getColor(R.color.backgroundDisabled)
                        )
                    )
                    if (isNotLast) {
                        append("\n")
                    }
                }
            }
        }
    }
}
