package org.p2p.wallet.moonpay.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.bottomsheet.BaseDoneBottomSheet
import org.p2p.wallet.databinding.DialogBuyDetailsPartBinding
import org.p2p.wallet.moonpay.model.BuyViewData
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.withArgs

private const val ARG_BUY_DATA = "ARG_BUY_DATA"

class BuyDetailsBottomSheet : BaseDoneBottomSheet() {

    companion object {
        fun show(
            fm: FragmentManager,
            title: String,
            buyData: BuyViewData,
            requestKey: String = ARG_REQUEST_KEY,
            resultKey: String = ARG_RESULT_KEY
        ) = BuyDetailsBottomSheet().withArgs(
            ARG_TITLE to title,
            ARG_BUY_DATA to buyData,
            ARG_REQUEST_KEY to requestKey,
            ARG_RESULT_KEY to resultKey
        ).show(fm, BuyDetailsBottomSheet::javaClass.name)
    }

    private val buyData: BuyViewData by args(ARG_BUY_DATA)

    lateinit var binding: DialogBuyDetailsPartBinding

    override fun onCreateInnerView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogBuyDetailsPartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showData(buyData)
    }

    override fun onStart() {
        super.onStart()
        BottomSheetBehavior.from(requireView().parent as View).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    private fun showData(viewData: BuyViewData) = with(binding) {
        optionsTextViewPrice.labelText = getString(R.string.buy_token_price, viewData.tokenSymbol)
        optionsTextViewPurchaseCost.labelText = getString(R.string.buy_token_purchase_cost, viewData.tokenSymbol)
        optionsTextViewAccountCreation.labelText = getString(R.string.buy_account_creation, viewData.tokenSymbol)

        optionsTextViewPrice.setValueText(viewData.priceText)
        optionsTextViewProcessingFee.setValueText(viewData.processingFeeText)
        optionsTextViewNetworkFee.setValueText(viewData.networkFeeText)
        optionsTextViewAccountCreation.setValueText(viewData.accountCreationCostText)
        viewData.purchaseCostText?.let {
            optionsTextViewPurchaseCost.setValueText(it)
        }

        optionsTextViewTotal.setValueText(viewData.totalText)
        overrideColors()
    }

    private fun overrideColors() = with(binding) {
        optionsTextViewPrice.apply {
            setLabelTextColor(getColor(R.color.text_mountain))
            setValueTextColor(getColor(R.color.text_mountain))
        }
        optionsTextViewPurchaseCost.setLabelTextColor(getColor(R.color.text_mountain))
        optionsTextViewAccountCreation.setLabelTextColor(getColor(R.color.text_mountain))
        optionsTextViewProcessingFee.setLabelTextColor(getColor(R.color.text_mountain))
        optionsTextViewNetworkFee.setLabelTextColor(getColor(R.color.text_mountain))
    }

    override fun getResult(): Any? = null
}
