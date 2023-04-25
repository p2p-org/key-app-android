package org.p2p.wallet.bridge.claim.ui.dialogs

import androidx.fragment.app.FragmentManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.math.BigDecimal
import org.p2p.core.utils.Constants
import org.p2p.core.utils.asUsd
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.bridge.claim.model.ClaimDetails
import org.p2p.wallet.bridge.model.BridgeAmount
import org.p2p.wallet.common.ui.bottomsheet.BaseDoneBottomSheet
import org.p2p.wallet.databinding.DialogClaimInfoBinding
import org.p2p.wallet.databinding.ItemClaimDetailsPartBinding
import org.p2p.wallet.databinding.ItemInfoImageDoubleTextBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.withArgs

private const val ARG_CLAIM_DETAILS = "ARG_CLAIM_DETAILS"

class ClaimInfoBottomSheet : BaseDoneBottomSheet() {

    companion object {
        fun show(
            fm: FragmentManager,
            claimDetails: ClaimDetails
        ) {
            ClaimInfoBottomSheet()
                .withArgs(ARG_CLAIM_DETAILS to claimDetails)
                .show(fm, ClaimInfoBottomSheet::javaClass.name)
        }
    }

    private var viewBinding: DialogClaimInfoBinding? = null
    private val binding get() = viewBinding!!

    private val claimDetails: ClaimDetails by args(ARG_CLAIM_DETAILS)

    override fun onCreateInnerView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = DialogClaimInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDoneButtonVisibility(isVisible = false)
        with(binding) {
            layoutFreeTransactions.bindLayoutFreeTransactions()
            layoutWillGetAmount.bindDetailsLineWithFee(
                title = getString(R.string.bridge_info_you_will_get),
                fee = claimDetails.willGetAmount,
                isFree = false
            )
            layoutNetworkFee.bindDetailsLineWithFee(
                title = getString(R.string.bridge_info_network_fee),
                fee = claimDetails.networkFee,
                isFree = claimDetails.isFree
            )
            layoutAccountCreationFee.bindDetailsLineWithFee(
                title = getString(R.string.bridge_info_account_creation_fee),
                fee = claimDetails.accountCreationFee,
                isFree = claimDetails.isFree
            )
            layoutBridgeFee.bindDetailsLineWithFee(
                title = getString(R.string.bridge_info_bridge_fee),
                fee = claimDetails.bridgeFee,
                isFree = claimDetails.isFree
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    private fun ItemInfoImageDoubleTextBinding.bindLayoutFreeTransactions() {
        root.setBackgroundResource(R.drawable.bg_rounded_solid_cloud_16)
        imageViewIcon.setImageResource(R.drawable.ic_lightning)
        textViewTitle.setText(R.string.how_to_claim_for_free_title)
        textViewSubtitle.text = getString(
            R.string.bridge_info_free_transaction_message,
            claimDetails.minAmountForFreeFee.toBigInteger()
        )
    }

    private fun ItemClaimDetailsPartBinding.bindDetailsLineWithFee(
        title: String,
        fee: BridgeAmount,
        isFree: Boolean
    ) {
        textViewTitle.text = title
        if (isFree) {
            textViewFiatAmount.text = getString(R.string.bridge_info_transaction_free)
            textViewTokenAmount.text = getString(R.string.bridge_claim_fees_free)
            textViewTokenAmount.setTextColorRes(R.color.text_mint)
        } else {
            textViewFiatAmount.text = fee.formattedFiatAmount ?: BigDecimal.ZERO.asUsd()
            textViewTokenAmount.text = fee.formattedTokenAmount ?: Constants.ZERO_AMOUNT
            textViewTokenAmount.setTextColorRes(R.color.text_night)
        }
    }
}
