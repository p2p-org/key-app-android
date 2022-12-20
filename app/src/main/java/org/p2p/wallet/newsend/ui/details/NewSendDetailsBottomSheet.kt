package org.p2p.wallet.newsend.ui.details

import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.koin.android.ext.android.inject
import org.p2p.core.token.Token
import org.p2p.uikit.utils.SpanUtils
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.toast
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogNewSendDetailsBinding
import org.p2p.wallet.send.model.SendFeeTotal
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrGone

private const val ARG_SEND_FEE_TOTAL = "ARG_SEND_FEE_TOTAL"
private const val ARG_REQUEST_KEY = "ARG_REQUEST_KEY"
private const val ARG_RESULT_KEY_FEE = "ARG_RESULT_KEY_FEE"
private const val ARG_RESULT_KEY_FEE_PAYER_TOKENS = "ARG_RESULT_KEY_FEE_PAYER_TOKENS"

class NewSendDetailsBottomSheet :
    BaseMvpBottomSheet<NewSendDetailsContract.View, NewSendDetailsContract.Presenter>(
        R.layout.dialog_new_send_details
    ),
    NewSendDetailsContract.View {

    companion object {
        fun show(
            fm: FragmentManager,
            totalFee: SendFeeTotal,
            requestKey: String,
            feeResultKey: String,
            feePayerTokensResultKey: String,
        ) = NewSendDetailsBottomSheet()
            .withArgs(
                ARG_SEND_FEE_TOTAL to totalFee,
                ARG_REQUEST_KEY to requestKey,
                ARG_RESULT_KEY_FEE to feeResultKey,
                ARG_RESULT_KEY_FEE_PAYER_TOKENS to feePayerTokensResultKey
            )
            .show(fm, NewSendDetailsBottomSheet::javaClass.name)
    }

    override val presenter: NewSendDetailsContract.Presenter by inject()

    private lateinit var binding: DialogNewSendDetailsBinding

    private val state: SendFeeTotal by args(ARG_SEND_FEE_TOTAL)
    private val feeResultKey: String by args(ARG_RESULT_KEY_FEE)
    private val feePayerTokensResultKey: String by args(ARG_RESULT_KEY_FEE_PAYER_TOKENS)
    private val requestKey: String by args(ARG_REQUEST_KEY)

    private val colorNight by unsafeLazy { getColor(R.color.text_night) }
    private val colorMountain by unsafeLazy { getColor(R.color.text_mountain) }
    private val colorMint by unsafeLazy { getColor(R.color.text_mint) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogNewSendDetailsBinding.inflate(inflater, container, false)
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
    }

    override fun showAccountCreationFeeLoading(isLoading: Boolean) {
        binding.imageViewAccountFeeInfo.isInvisible = isLoading
        binding.progressBarFees.isVisible = isLoading
    }

    override fun showNoTokensScreen(tokens: List<Token.Active>) {
        val sendFee = state.sendFee ?: return // can't be null

        val bundle = bundleOf(
            feeResultKey to sendFee,
            feePayerTokensResultKey to tokens
        )
        setFragmentResult(requestKey, bundle)
        dismissAllowingStateLoss()
    }

    private fun DialogNewSendDetailsBinding.setRecipientAddress() {
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

    private fun DialogNewSendDetailsBinding.setRecipientReceiveAmount() {
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

    private fun DialogNewSendDetailsBinding.setTransactionFee() {
        with(layoutTransactionFee) {
            imageViewIcon.setImageResource(R.drawable.ic_lightling)
            textViewTitle.text = getString(R.string.send_transactions_details_transaction_fee)
            textViewSubtitle.apply {
                val fee = state.sendFee
                text = if (fee != null && !fee.isTransactionFree) {
                    setTextColor(colorNight)
                    SpanUtils.highlightText(
                        commonText = fee.transactionFullFee,
                        highlightedText = fee.approxTransactionFeeUsd.orEmpty(),
                        color = colorMountain
                    )
                } else {
                    setTextColor(colorMint)
                    getString(R.string.send_free_fee_format, state.feeLimit.remaining)
                }
            }
        }
    }

    private fun DialogNewSendDetailsBinding.setAccountCreation() {
        groupAccountFee.isVisible = state.showAccountCreation
        imageViewIconAccountFee.setImageResource(R.drawable.ic_user)
        textViewTitleAccountFee.text = getString(R.string.send_transactions_details_account_fee)

        val fee = state.sendFee
        if (fee != null) {
            textViewSubtitleAccountFee.setTextColor(colorNight)
            textViewSubtitleAccountFee.text = SpanUtils.highlightText(
                commonText = fee.accountCreationFeeUsd,
                highlightedText = fee.approxAccountCreationFeeUsd.orEmpty(),
                color = colorMountain
            )
            imageViewAccountFeeInfo.setOnClickListener {
                presenter.findAlternativeFeePayerTokens(fee)
            }
        } else {
            textViewSubtitleAccountFee.setTextColor(colorMint)
            textViewSubtitleAccountFee.text = getString(R.string.send_free_fee_format, state.feeLimit.remaining)
        }
    }

    private fun DialogNewSendDetailsBinding.setTotal() {
        imageViewIconTotal.setImageResource(R.drawable.ic_receipt)
        textViewTitleTotal.text = getString(R.string.send_transactions_details_total)
        textViewSubtitleFirstTotal.text = state.getTotalCombined(colorMountain)

        textViewSubtitleSecondTotal withTextOrGone state.getFeesCombined(colorMountain)
    }
}
