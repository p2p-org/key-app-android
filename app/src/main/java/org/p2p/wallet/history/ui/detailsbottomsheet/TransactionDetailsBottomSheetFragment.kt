package org.p2p.wallet.history.ui.detailsbottomsheet

import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.common.glide.GlideManager
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
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

    private val glideManager: GlideManager by inject()

    override val presenter: TransactionDetailsBottomSheetContract.Presenter by inject {
        parametersOf(state)
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded

    override fun onStart() {
        super.onStart()
        BottomSheetBehavior.from(requireView().parent as View).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun showError(@StringRes messageId: Int) {
        showInfoDialog(
            titleRes = R.string.error_general_title,
            messageRes = messageId,
            primaryButtonRes = R.string.common_retry,
            primaryCallback = { presenter.load() },
            isCancelable = false
        )
    }

    override fun showTransferView(iconRes: Int) = with(binding) {
        transactionSwapImageView.isVisible = false
        with(transactionImageView) {
            isVisible = true
            setTransactionIcon(iconRes)
        }
    }

    override fun showSwapView(
        sourceIconUrl: String,
        destinationIconUrl: String
    ) = with(binding) {
        transactionImageView.isVisible = false
        with(transactionSwapImageView) {
            isVisible = true
            setSourceAndDestinationImages(
                glideManager,
                sourceIconUrl,
                destinationIconUrl
            )
        }
    }

    override fun showDate(date: String) {
        binding.dateTextView.text = date
    }

    override fun showStatus(status: TransactionStatus) {
        binding.statusTextView.setText(status.resValue)
        val color = when (status) {
            TransactionStatus.COMPLETED -> R.color.color_green
            TransactionStatus.PENDING -> R.color.systemWarningMain
            TransactionStatus.ERROR -> R.color.systemErrorMain
        }

        binding.transactionImageView.setStatus(status)
        binding.statusTextView.setTextColor(getColor(color))
    }

    override fun showSignature(signature: String) {
        with(binding) {
            transactionIdTextView.setOnClickListener {
                requireContext().copyToClipBoard(signature)
                toast(R.string.common_copied)
            }
            transactionIdTextView.text = signature.cutEnd()
            doneButton.setOnClickListener {
                dismiss()
            }
            detailsButton.setOnClickListener {
                val url = getString(R.string.solanaExplorer, signature)
                showUrlInCustomTabs(url)
            }
        }
    }

    override fun showAddresses(source: String, destination: String) {
        with(binding) {
            sourceAddressTextView.setOnClickListener {
                requireContext().copyToClipBoard(source)
                toast(R.string.common_copied)
            }
            sourceAddressTextView.text = source

            destinationAddressTextView.setOnClickListener {
                requireContext().copyToClipBoard(destination)
                toast(R.string.common_copied)
            }
            destinationAddressTextView.text = destination
        }
    }

    override fun showAmount(amountToken: String, amountUsd: String?) {
        with(binding) {
            amountTextTokenView.text = amountToken
            amountTextUsdView.text = amountUsd
            amountTextUsdView.isVisible = amountUsd != null
        }
    }

    override fun showFee(renBtcFee: String?) {
        with(binding) {
            if (renBtcFee.isNullOrEmpty()) {
                feesTextView.text = getString(R.string.transaction_details_fee_free)
                feesTextView.setTextColor(getColor(R.color.textIconActive))
            } else {
                feesTextView.setTextColor(getColor(R.color.textIconPrimary))
                feesTextView.text = renBtcFee
            }
        }
    }

    override fun showBlockNumber(blockNumber: String?) {
        binding.blockNumberTextView withTextOrGone (blockNumber)
        binding.blockNumberTitleTextView.isVisible = !blockNumber.isNullOrEmpty()
    }

    override fun showLoading(isLoading: Boolean) {
        with(binding) {
            scrollView.isVisible = !isLoading
            progressView.isVisible = isLoading
        }
    }
}
