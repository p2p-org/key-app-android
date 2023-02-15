package org.p2p.wallet.history.ui.details

import android.os.Bundle
import android.text.SpannableString
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.toast
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.bottomsheet.DrawableContainer
import org.p2p.wallet.databinding.FragmentTransactionTransferBinding
import org.p2p.wallet.transaction.model.HistoryTransactionStatus
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.cutEnd
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.showInfoDialog
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrGone
import org.p2p.wallet.utils.withTextOrInvisible

private const val EXTRA_TX_SIGNATURE = "EXTRA_TX_SIGNATURE"

class TransactionDetailsFragment :
    BaseMvpFragment<TransactionDetailsContract.View, TransactionDetailsContract.Presenter>(
        R.layout.fragment_transaction_transfer
    ),
    TransactionDetailsContract.View {

    companion object {
        fun create(txSignature: String) =
            TransactionDetailsFragment()
                .withArgs(EXTRA_TX_SIGNATURE to txSignature)
    }

    private val binding: FragmentTransactionTransferBinding by viewBinding()
    private val txSignature: String by args(EXTRA_TX_SIGNATURE)
    override val presenter: TransactionDetailsContract.Presenter by inject {
        parametersOf(txSignature)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { popBackStack() }
    }

    override fun showError(messageId: Int) {
        showInfoDialog(
            titleRes = R.string.error_general_title,
            messageRes = messageId,
            primaryButtonRes = R.string.common_retry,
            primaryCallback = { presenter.load() },
            isCancelable = false
        )
    }

    override fun showTitle(title: String) {
        binding.toolbar.title = title
    }

    override fun showDate(date: String) {
        binding.textViewDate.text = date
    }

    override fun showStatus(status: HistoryTransactionStatus) {
        binding.statusTextView.setText(status.resValue)
        val color = when (status) {
            HistoryTransactionStatus.COMPLETED -> R.color.color_green
            HistoryTransactionStatus.PENDING -> R.color.systemWarningMain
            HistoryTransactionStatus.ERROR -> R.color.systemErrorMain
        }

        binding.statusColorView.setBackgroundColor(getColor(color))
    }

    override fun showSourceInfo(iconContainer: DrawableContainer, primaryInfo: String, secondaryInfo: String?) {
        with(binding) {
            iconContainer.applyTo(sourceImageView)
            sourceTextView.text = primaryInfo
            sourceSecondaryTextView withTextOrInvisible secondaryInfo
        }
    }

    override fun showDestinationInfo(iconContainer: DrawableContainer, primaryInfo: String, secondaryInfo: String?) {
        with(binding) {
            iconContainer.applyTo(destinationImageView)
            destinationTextView.text = primaryInfo
            destinationSecondaryTextView withTextOrInvisible secondaryInfo
        }
    }

    override fun showSignature(signature: String) {
        with(binding) {
            transactionIdTitleTextView.setOnClickListener {
                requireContext().copyToClipBoard(signature)
                toast(R.string.common_copied)
            }
            transactionIdTextView.text = signature.cutEnd()
            explorerView.clipToOutline = true
            explorerView.setOnClickListener {
                val url = getString(R.string.solanaExplorer, signature)
                showUrlInCustomTabs(url)
            }
        }
    }

    override fun showAddresses(source: String, destination: String) {
        with(binding) {
            sourceTitleTextView.setOnClickListener {
                requireContext().copyToClipBoard(source)
                toast(R.string.common_copied)
            }
            sourceAddressTextView.text = source

            destinationTitleTextView.setOnClickListener {
                requireContext().copyToClipBoard(destination)
                toast(R.string.common_copied)
            }
            destinationAddressTextView.text = destination
        }
    }

    override fun showAmount(@StringRes label: Int, amount: CharSequence) {
        with(binding) {
            amountLabelTextView.setText(label)
            amountTextView.text = amount
        }
    }

    override fun showLiquidityProviderFees(sourceFee: SpannableString, destinationFee: SpannableString) {
        // todo: add fees
    }

    override fun showFee(renBtcFee: String?) {
        with(binding) {
            if (renBtcFee.isNullOrEmpty()) {
                feesTextView.text = getString(R.string.send_free_transaction)
                feesTextView.setTextColor(getColor(R.color.systemSuccessMain))
                freeTextView.isVisible = true
            } else {
                feesTextView.text = renBtcFee
            }
        }
    }

    override fun showBlockNumber(blockNumber: String?) {
        binding.blockNumberTextView withTextOrGone blockNumber
        binding.blockNumberTitleTextView.isVisible = !blockNumber.isNullOrEmpty()
    }

    override fun showLoading(isLoading: Boolean) {
        with(binding) {
            scrollView.isVisible = !isLoading
            progressView.isVisible = isLoading
        }
    }
}
