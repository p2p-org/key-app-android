package org.p2p.wallet.history.ui.detailsbottomsheet

import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.glide.GlideManager
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogHistoryTransactionDetailsBinding
import org.p2p.wallet.transaction.model.HistoryTransactionStatus
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.setStatus
import org.p2p.wallet.utils.showInfoDialog
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_TX_SIGNATURE = "EXTRA_TX_SIGNATURE"

class HistoryTransactionDetailsBottomSheetFragment :
    BaseMvpBottomSheet<HistoryTransactionDetailsContract.View, HistoryTransactionDetailsContract.Presenter>(
        R.layout.dialog_history_transaction_details
    ),
    HistoryTransactionDetailsContract.View {

    companion object {
        fun show(fragmentManager: FragmentManager, signature: String) {
            HistoryTransactionDetailsBottomSheetFragment()
                .withArgs(EXTRA_TX_SIGNATURE to signature)
                .show(fragmentManager, HistoryTransactionDetailsBottomSheetFragment::javaClass.name)
        }

        fun hide(fragmentManager: FragmentManager) {
            val dialog = fragmentManager.findFragmentByTag(HistoryTransactionDetailsBottomSheetFragment::javaClass.name)
            (dialog as? HistoryTransactionDetailsBottomSheetFragment)?.dismissAllowingStateLoss()
        }
    }

    private val state: String by args(EXTRA_TX_SIGNATURE)

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
        binding.textViewDate.text = date
    }

    override fun showStatus(status: HistoryTransactionStatus) {
        binding.textViewStatus.setText(status.resValue)
        val color = when (status) {
            HistoryTransactionStatus.COMPLETED -> R.color.color_green
            HistoryTransactionStatus.PENDING -> R.color.systemWarningMain
            HistoryTransactionStatus.ERROR -> R.color.systemErrorMain
        }

        binding.transactionImageView.setStatus(status)
        binding.textViewStatus.setTextColor(getColor(color))
    }

    override fun showTransactionId(signature: String) {
        with(binding) {
            textViewTransactionId.text = signature
            textViewTransactionId.setOnClickListener {
                requireContext().copyToClipBoard(signature)
                showUiKitSnackBar(messageResId = R.string.transaction_details_transaction_id_copied)
            }
            buttonDone.setOnClickListener {
                dismiss()
            }
            buttonDetails.setOnClickListener {
                val url = getString(R.string.solanaExplorer, signature)
                showUrlInCustomTabs(url)
            }
        }
    }

    override fun showAddresses(source: String, destination: String) = with(binding) {
        containerActor.isVisible = false
        textViewSourceAddress.setOnClickListener {
            requireContext().copyToClipBoard(source)
            showUiKitSnackBar(messageResId = R.string.transaction_details_sender_address_copied)
        }
        textViewSourceAddress.text = source

        textViewDestinationAddress.setOnClickListener {
            requireContext().copyToClipBoard(destination)
            showUiKitSnackBar(messageResId = R.string.transaction_details_receiver_address_copied)
        }
        textViewDestinationAddress.text = destination
    }

    override fun showSenderAddress(senderAddress: Base58String, senderUsername: String?) = with(binding) {
        containerSource.isVisible = false
        containerDestination.isVisible = false

        containerActor.isVisible = true

        textViewActorAddressValue.text = senderAddress.base58Value
        textViewActorAddressValue.setOnClickListener {
            requireContext().copyToClipBoard(senderAddress.base58Value)
            showUiKitSnackBar(
                messageResId = R.string.transaction_details_sender_address_copied,
                actionButtonResId = R.string.common_hide,
                actionBlock = Snackbar::dismiss
            )
        }

        if (senderUsername != null) {
            containerUsername.isVisible = true
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

        textViewActorAddressValue.text = receiverAddress.base58Value
        textViewActorAddressValue.setOnClickListener {
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
            textViewFees.setTextColorRes(R.color.textIconActive)
            textViewFees.text = getString(R.string.transaction_details_fee_free)
        } else {
            textViewFees.setTextColorRes(R.color.textIconPrimary)
            textViewFees.text = renBtcFee
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.scrollView.isGone = isLoading
        binding.progressView.isVisible = isLoading
    }
}
