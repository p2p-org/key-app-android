package org.p2p.wallet.history.ui.detailsbottomsheet

import androidx.annotation.StringRes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.uikit.glide.GlideManager
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogHistoryTransactionDetailsBinding
import org.p2p.wallet.history.model.TransactionDetailsLaunchState
import org.p2p.wallet.transaction.model.TransactionStatus
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.setStatus
import org.p2p.wallet.utils.showInfoDialog
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrGone

private const val EXTRA_STATE = "EXTRA_STATE"

class HistoryTransactionDetailsBottomSheetFragment :
    BaseMvpBottomSheet<HistoryTransactionDetailsContract.View, HistoryTransactionDetailsContract.Presenter>(
        R.layout.dialog_history_transaction_details
    ),
    HistoryTransactionDetailsContract.View {

    companion object {
        fun show(fragmentManager: FragmentManager, state: TransactionDetailsLaunchState) {
            HistoryTransactionDetailsBottomSheetFragment()
                .withArgs(EXTRA_STATE to state)
                .show(fragmentManager, HistoryTransactionDetailsBottomSheetFragment::javaClass.name)
        }

        fun hide(fragmentManager: FragmentManager) {
            val dialog = fragmentManager.findFragmentByTag(HistoryTransactionDetailsBottomSheetFragment::javaClass.name)
            (dialog as? HistoryTransactionDetailsBottomSheetFragment)?.dismissAllowingStateLoss()
        }
    }

    private val state: TransactionDetailsLaunchState by args(EXTRA_STATE)

    private val binding: DialogHistoryTransactionDetailsBinding by viewBinding()

    private val glideManager: GlideManager by inject()

    override val presenter: HistoryTransactionDetailsContract.Presenter by inject { parametersOf(state) }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded

    override fun onStart() {
        super.onStart()
        BottomSheetBehavior.from(requireView().parent as View).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    override fun showError(@StringRes messageId: Int) {
        showInfoDialog(
            titleRes = R.string.error_general_title,
            messageRes = messageId,
            primaryButtonRes = R.string.common_retry,
            primaryCallback = presenter::load,
            isCancelable = false
        )
    }

    override fun showTransferView(iconRes: Int) = with(binding) {
        transactionSwapImageView.isVisible = false
        transactionImageView.isVisible = true
        transactionImageView.setTransactionIcon(iconRes)
    }

    override fun showSwapView(sourceIconUrl: String, destinationIconUrl: String) = with(binding) {
        transactionImageView.isVisible = false
        transactionSwapImageView.isVisible = true
        transactionSwapImageView.setSourceAndDestinationImages(
            glideManager = glideManager,
            sourceIconUrl = sourceIconUrl,
            destinationIconUrl = destinationIconUrl
        )
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
                showUiKitSnackBar(messageResId = R.string.common_copied)
            }
            transactionIdTextView.text = signature
            doneButton.setOnClickListener {
                dismiss()
            }
            detailsButton.setOnClickListener {
                val url = getString(R.string.solanaExplorer, signature)
                showUrlInCustomTabs(url)
            }
        }
    }

    override fun showAddresses(source: String, destination: String) = with(binding) {
        containerActor.isVisible = false
        sourceAddressTextView.setOnClickListener {
            requireContext().copyToClipBoard(source)
            showUiKitSnackBar(messageResId = R.string.transaction_details_sender_address_copied)
        }
        sourceAddressTextView.text = source

        destinationAddressTextView.setOnClickListener {
            requireContext().copyToClipBoard(destination)
            showUiKitSnackBar(messageResId = R.string.transaction_details_receiver_address_copied)
        }
        destinationAddressTextView.text = destination
    }

    override fun showSenderAddress(senderAddress: Base58String, senderUsername: String?) = with(binding) {
        containerSource.isVisible = false
        containerDestination.isVisible = false

        containerActor.isVisible = true

        actorAddressValueTextView.text = senderAddress.base58Value
        actorAddressValueTextView.setOnClickListener {
            requireContext().copyToClipBoard(senderAddress.base58Value)
            showUiKitSnackBar(
                messageResId = R.string.transaction_details_sender_address_copied,
                actionButtonResId = R.string.common_hide,
                actionBlock = Snackbar::dismiss
            )
        }

        if (senderUsername != null) {
            textViewUsernameValue.text = senderUsername
            textViewUsernameValue.setOnClickListener {
                requireContext().copyToClipBoard(senderUsername)
                showUiKitSnackBar(
                    messageResId = R.string.transaction_details_sender_username_copied,
                    actionButtonResId = R.string.common_hide,
                    actionBlock = Snackbar::dismiss
                )
            }
        }
    }

    override fun showReceiverAddress(receiverAddress: Base58String, receiverUsername: String?) = with(binding) {
        containerSource.isVisible = false
        containerDestination.isVisible = false

        containerActor.isVisible = true

        actorAddressValueTextView.text = receiverAddress.base58Value
        actorAddressValueTextView.setOnClickListener {
            requireContext().copyToClipBoard(receiverAddress.base58Value)
            showUiKitSnackBar(
                messageResId = R.string.transaction_details_receiver_address_copied,
                actionButtonResId = R.string.common_hide,
                actionBlock = Snackbar::dismiss
            )
        }

        if (receiverUsername != null) {
            containerUsername.isVisible = true
            textViewUsernameValue.text = receiverUsername
            textViewUsernameValue.setOnClickListener {
                requireContext().copyToClipBoard(receiverUsername)
                showUiKitSnackBar(
                    messageResId = R.string.transaction_details_receiver_username_copied,
                    actionButtonResId = R.string.common_hide,
                    actionBlock = Snackbar::dismiss
                )
            }
        } else {
            containerUsername.isVisible = false
        }
    }

    override fun showAmount(amountToken: String, amountUsd: String?) = with(binding) {
        amountTextTokenView.text = amountToken
        amountTextUsdView.text = amountUsd
        amountTextUsdView.isVisible = amountUsd != null
    }

    override fun showFee(renBtcFee: String?) = with(binding) {
        if (renBtcFee.isNullOrEmpty()) {
            feesTextView.setTextColorRes(R.color.textIconActive)
            feesTextView.text = getString(R.string.transaction_details_fee_free)
        } else {
            feesTextView.setTextColorRes(R.color.textIconPrimary)
            feesTextView.text = renBtcFee
        }
    }

    override fun showBlockNumber(blockNumber: String?) {
        binding.blockNumberTextView.withTextOrGone(blockNumber)
        binding.blockNumberTitleTextView.isVisible = !blockNumber.isNullOrEmpty()
    }

    override fun showLoading(isLoading: Boolean) {
        binding.scrollView.isGone = isLoading
        binding.progressView.isVisible = isLoading
    }
}
