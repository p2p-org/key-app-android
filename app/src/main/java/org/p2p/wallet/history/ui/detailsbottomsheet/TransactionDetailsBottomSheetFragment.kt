package org.p2p.wallet.history.ui.detailsbottomsheet

import android.text.SpannableString
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.common.ui.bottomsheet.DrawableContainer
import org.p2p.wallet.databinding.DialogTransactionDetailsBinding
import org.p2p.wallet.history.model.TransactionDetailsLaunchState
import org.p2p.wallet.transaction.model.TransactionStatus
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.cutEnd
import org.p2p.wallet.utils.getColor
import org.p2p.wallet.utils.showInfoDialog
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.toast
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrGone
import org.p2p.wallet.utils.withTextOrInvisible

private const val EXTRA_STATE = "EXTRA_STATE"

class TransactionDetailsBottomSheetFragment :
    BaseMvpBottomSheet<TransactionDetailsBottomSheetContract.View, TransactionDetailsBottomSheetContract.Presenter>(
        R.layout.dialog_transaction_details
    ),
    TransactionDetailsBottomSheetContract.View {

    companion object {
        fun show(fragmentManager: FragmentManager, state: TransactionDetailsLaunchState) {
            TransactionDetailsBottomSheetFragment()
                .withArgs(EXTRA_STATE to state)
                .show(fragmentManager, TransactionDetailsBottomSheetFragment::javaClass.name)
        }

        fun hide(fragmentManager: FragmentManager) {
            val dialog = fragmentManager.findFragmentByTag(TransactionDetailsBottomSheetFragment::javaClass.name)
            (dialog as? TransactionDetailsBottomSheetFragment)?.dismissAllowingStateLoss()
        }
    }

    private val state: TransactionDetailsLaunchState by args(EXTRA_STATE)

    private val binding: DialogTransactionDetailsBinding by viewBinding()

    override val presenter: TransactionDetailsBottomSheetContract.Presenter by inject {
        parametersOf(state)
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

    override fun showDate(date: String) {
        binding.dateTextView.text = date
    }

    override fun showStatus(status: TransactionStatus) {
        binding.statusTextView.setText(status.resValue)
        val color = when (status) {
            TransactionStatus.COMPLETED -> R.color.colorGreen
            TransactionStatus.PENDING -> R.color.systemWarningMain
            TransactionStatus.ERROR -> R.color.systemErrorMain
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
