package org.p2p.wallet.claim.ui.dialogs

import androidx.fragment.app.FragmentManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.claim.model.ClaimDetails
import org.p2p.wallet.claim.model.ClaimFee
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
            ClaimInfoBottomSheet().withArgs(
                ARG_CLAIM_DETAILS to claimDetails
            ).show(
                fm, ClaimInfoBottomSheet::javaClass.name
            )
        }
    }

    private lateinit var binding: DialogClaimInfoBinding

    private val claimDetails: ClaimDetails by args(ARG_CLAIM_DETAILS)

    override fun onCreateInnerView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogClaimInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDoneButtonVisibility(isVisible = false)
        with(binding) {
            layoutFreeTransactions.bindLayoutFreeTransactions()
            layoutWillGetAmount.bindDetailsLineWithFee(
                title = getString(R.string.claim_info_you_will_get),
                fee = claimDetails.willGet
            )
            layoutNetworkFee.bindDetailsLineWithFee(
                title = getString(R.string.claim_info_network_fee),
                fee = claimDetails.networkFee
            )
            layoutAccountCreationFee.bindDetailsLineWithFee(
                title = getString(R.string.claim_info_account_creation_fee),
                fee = claimDetails.accountCreationFee
            )
            layoutBridgeFee.bindDetailsLineWithFee(
                title = getString(R.string.claim_info_bridge_fee),
                fee = claimDetails.bridgeFee
            )
        }
    }

    private fun ItemInfoImageDoubleTextBinding.bindLayoutFreeTransactions() {
        root.setBackgroundResource(R.drawable.bg_rounded_solid_cloud_16)
        imageViewIcon.setImageResource(R.drawable.ic_lightning)
        textViewTitle.setText(R.string.free_transactions_title)
        textViewSubtitle.text = getString(R.string.claim_info_free_transaction_message)
    }

    private fun ItemClaimDetailsPartBinding.bindDetailsLineWithFee(title: String, fee: ClaimFee) {
        textViewTitle.text = title
        textViewFiatAmount.text = fee.formattedFiatAmount ?: getString(R.string.claim_info_transaction_free)
        val formattedTokenAmount = fee.formattedTokenAmount
        if (formattedTokenAmount == null) {
            textViewTokenAmount.text = getString(R.string.claiming_fees_free)
            textViewTokenAmount.setTextColorRes(R.color.text_mint)
        } else {
            textViewTokenAmount.text = formattedTokenAmount
            textViewTokenAmount.setTextColorRes(R.color.text_night)
        }
    }
}
