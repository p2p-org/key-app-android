package org.p2p.wallet.send.ui.dialogs

import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.p2p.uikit.utils.SpanUtils
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.toast
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.bottomsheet.BaseDoneBottomSheet
import org.p2p.wallet.databinding.DialogSendTransactionsDetailsBinding
import org.p2p.wallet.send.model.SendSolanaFee
import org.p2p.wallet.send.model.SendFeeTotal
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.withArgs

private const val ARG_SEND_STATE = "ARG_SEND_STATE"

class SendTransactionsDetailsBottomSheet : BaseDoneBottomSheet() {

    companion object {
        fun show(
            fm: FragmentManager,
            state: SendFeeTotal,
        ) = SendTransactionsDetailsBottomSheet()
            .withArgs(ARG_SEND_STATE to state)
            .show(fm, SendTransactionsDetailsBottomSheet::javaClass.name)
    }

    private lateinit var binding: DialogSendTransactionsDetailsBinding

    private val state: SendFeeTotal by args(ARG_SEND_STATE)

    private val colorNight = getColor(R.color.text_night)
    private val colorMountain = getColor(R.color.text_mountain)
    private val colorMint = getColor(R.color.text_mint)

    override fun onCreateInnerView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogSendTransactionsDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            setRecipientAddress()
            setRecipientReceiveAmount()
            setTransactionFee()
            setAccountCreation()
            setTotal()
        }

        setDoneButtonVisibility(isVisible = false)
    }

    private fun DialogSendTransactionsDetailsBinding.setRecipientAddress() {
        val address = state.recipientAddress
        with(layoutAddress) {
            imageViewIcon.setImageResource(R.drawable.ic_wallet_home)
            textViewTitle.text = getString(R.string.send_transactions_details_address)
            textViewSubtitle.text = address
            root.setOnLongClickListener {
                requireContext().copyToClipBoard(address)
                toast(R.string.common_copied)
                true
            }
        }
    }

    private fun DialogSendTransactionsDetailsBinding.setRecipientReceiveAmount() {
        val color = getColor(R.color.text_mountain)
        with(layoutReceiveAmount) {
            imageViewIcon.setImageResource(R.drawable.ic_receive)
            textViewTitle.text = getString(R.string.send_transactions_details_gets)
            textViewSubtitle.text = SpanUtils.highlightText(
                commonText = state.fullReceive,
                highlightedText = state.approxReceive,
                color = color
            )
        }
    }

    private fun DialogSendTransactionsDetailsBinding.setTransactionFee() {
        with(layoutTransactionFee) {
            imageViewIcon.setImageResource(R.drawable.ic_lightling)
            textViewTitle.text = getString(R.string.send_transactions_details_transaction_fee)
            textViewSubtitle.apply {
                when (val fee = state.fee) {
                    is SendSolanaFee.SolanaFee -> {
                        setTextColor(colorNight)
                        text = SpanUtils.highlightText(
                            commonText = fee.accountCreationFullFee,
                            highlightedText = fee.approxAccountCreationFeeUsd.orEmpty(),
                            color = colorMountain
                        )
                    }
                    else -> {
                        setTextColor(colorMint)
                        setText(R.string.send_free_transaction)
                        // TODO PWN-6092 make counting of free transactions and set it here!
                    }
                }
            }
        }
    }

    private fun DialogSendTransactionsDetailsBinding.setAccountCreation() {
        groupAccountFee.isVisible = state.showAccountCreation
        imageViewAccountFeeInfo.setImageResource(R.drawable.ic_user)
        textViewTitleAccountFee.text = getString(R.string.send_transactions_details_account_fee)
        textViewSubtitleAccountFee.text = getString(R.string.send_transactions_details_account_fee)

        textViewSubtitleAccountFee.apply {
            when (val fee = state.fee) {
                is SendSolanaFee.SolanaFee -> {
                    setTextColor(colorNight)
                    text = SpanUtils.highlightText(
                        commonText = fee.accountCreationFullFee,
                        highlightedText = fee.approxAccountCreationFeeUsd.orEmpty(),
                        color = colorMountain
                    )
                }
                else -> {
                    setTextColor(colorMint)
                    setText(R.string.send_free_transaction)
                    // TODO PWN-6092 make counting of free transactions and set it here!
                }
            }
        }

        imageViewAccountFeeInfo.setOnClickListener {
            // TODO PWN-6092 make info screens to open!
        }
    }

    private fun DialogSendTransactionsDetailsBinding.setTotal() {
        imageViewIconTotal.setImageResource(R.drawable.ic_receipt)
        textViewTitleTotal.text = getString(R.string.send_transactions_details_total)
        textViewSubtitleFirstTotal.text = SpanUtils.highlightText(
            commonText = state.fullTotal,
            highlightedText = state.approxTotalUsd.orEmpty(),
            color = colorMountain
        )
        textViewSubtitleSecondTotal.isVisible = false
        // TODO PWN-6092 make info screens to open!
    }
}
