package org.p2p.wallet.moonpay.ui.bottomsheet

import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.bottomsheet.BaseDoneBottomSheet
import org.p2p.wallet.databinding.DialogBuyDetailsPartBinding
import org.p2p.wallet.moonpay.model.BuyDetailsState
import org.p2p.wallet.moonpay.model.BuyViewData
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.withArgs

private const val ARG_BUY_STATE = "ARG_BUY_STATE"

class BuyDetailsBottomSheet : BaseDoneBottomSheet() {

    companion object {
        fun show(
            fm: FragmentManager,
            title: String,
            state: BuyDetailsState,
            requestKey: String = ARG_REQUEST_KEY,
            resultKey: String = ARG_RESULT_KEY
        ) = BuyDetailsBottomSheet().withArgs(
            ARG_TITLE to title,
            ARG_BUY_STATE to state,
            ARG_REQUEST_KEY to requestKey,
            ARG_RESULT_KEY to resultKey
        ).show(fm, BuyDetailsBottomSheet::javaClass.name)
    }

    private val state: BuyDetailsState by args(ARG_BUY_STATE)

    private lateinit var binding: DialogBuyDetailsPartBinding

    override fun onCreateInnerView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogBuyDetailsPartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (val state = state) {
            is BuyDetailsState.Valid -> showData(state.data)
            is BuyDetailsState.MinAmountError -> showErrorState(state)
        }
    }

    private fun showErrorState(state: BuyDetailsState.MinAmountError) {
        binding.containerContent.isVisible = false
        binding.textViewError.isVisible = true
        binding.textViewError.text = getString(R.string.buy_min_amount_error_format, state.amount)
    }

    private fun showData(viewData: BuyViewData) = with(binding) {
        binding.containerContent.isVisible = true
        binding.textViewError.isVisible = false

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
