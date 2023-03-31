package org.p2p.wallet.bridge.send.ui.dialog

import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.bridge.model.BridgeAmount
import org.p2p.wallet.bridge.send.ui.model.BridgeFeeDetails
import org.p2p.wallet.common.ui.bottomsheet.BaseDoneBottomSheet
import org.p2p.wallet.databinding.DialogBridgeSendInfoBinding
import org.p2p.wallet.databinding.ItemClaimDetailsPartBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.withArgs

private const val ARG_FEE_DETAILS_DETAILS = "ARG_FEE_DETAILS_DETAILS"

class BridgeSendFeeBottomSheet : BaseDoneBottomSheet() {

    companion object {
        fun show(
            fm: FragmentManager,
            title: String,
            bridgeFeeDetails: BridgeFeeDetails
        ) {
            BridgeSendFeeBottomSheet()
                .withArgs(
                    ARG_TITLE to title,
                    ARG_FEE_DETAILS_DETAILS to bridgeFeeDetails
                )
                .show(fm, BridgeSendFeeBottomSheet::javaClass.name)
        }
    }

    private var viewBinding: DialogBridgeSendInfoBinding? = null
    private val binding get() = viewBinding!!

    private val bridgeFeeDetails: BridgeFeeDetails by args(ARG_FEE_DETAILS_DETAILS)

    override fun onCreateInnerView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = DialogBridgeSendInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDoneButtonVisibility(isVisible = false)
        with(binding) {
            layoutAddress.bindTitleValueLine(
                title = getString(R.string.bridge_send_fee_details_address),
                value = bridgeFeeDetails.recipientAddress
            )
            layoutWillGetAmount.bindDetailsLineWithFee(
                title = getString(R.string.bridge_send_fee_details_gets),
                fee = bridgeFeeDetails.willGetAmount
            )
            layoutNetworkFee.bindDetailsLineWithFee(
                title = getString(R.string.bridge_send_fee_details_network),
                fee = bridgeFeeDetails.networkFee
            )
            layoutBridgeFee.bindDetailsLineWithFee(
                title = getString(R.string.bridge_send_fee_details_wormhole),
                fee = bridgeFeeDetails.bridgeFee
            )
            layoutTotal.bindDetailsLineWithFee(
                title = getString(R.string.bridge_send_fee_details_total),
                fee = bridgeFeeDetails.total
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    private fun ItemClaimDetailsPartBinding.bindTitleValueLine(title: String, value: String) {
        textViewTitle.text = title
        textViewTokenAmount.text = value
        textViewFiatAmount.isVisible = false
    }

    private fun ItemClaimDetailsPartBinding.bindDetailsLineWithFee(title: String, fee: BridgeAmount) {
        textViewTitle.text = title
        textViewFiatAmount.text = fee.formattedFiatAmount ?: getString(R.string.bridge_info_transaction_free)
        val formattedTokenAmount = fee.formattedTokenAmount
        if (formattedTokenAmount == null) {
            textViewTokenAmount.text = getString(R.string.bridge_claim_fees_free)
            textViewTokenAmount.setTextColorRes(R.color.text_mint)
        } else {
            textViewTokenAmount.text = formattedTokenAmount
            textViewTokenAmount.setTextColorRes(R.color.text_night)
        }
    }
}
