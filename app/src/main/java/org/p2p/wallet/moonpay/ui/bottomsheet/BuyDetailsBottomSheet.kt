package org.p2p.wallet.moonpay.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
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

    private fun showData(viewData: BuyViewData) = with(binding) {
        priceView.labelText = getString(R.string.buy_token_price, viewData.tokenSymbol)
        purchaseCostView.labelText = getString(R.string.buy_token_purchase_cost, viewData.tokenSymbol)
        accountCreationView.labelText = getString(R.string.buy_account_creation, viewData.tokenSymbol)

        priceView.setValueText(viewData.priceText)
        processingFeeView.setValueText(viewData.processingFeeText)
        networkFeeView.setValueText(viewData.networkFeeText)
        accountCreationView.setValueText(viewData.accountCreationCostText)
        viewData.purchaseCostText?.let {
            purchaseCostView.setValueText(it)
        }

        totalView.setValueText(viewData.totalText)
        overrideColors()
    }

    private fun overrideColors() = with(binding) {
        priceView.apply {
            setLabelTextColor(getColor(R.color.text_mountain))
            setValueTextColor(getColor(R.color.text_mountain))
        }
        purchaseCostView.setLabelTextColor(getColor(R.color.text_mountain))
        accountCreationView.setLabelTextColor(getColor(R.color.text_mountain))
        processingFeeView.setLabelTextColor(getColor(R.color.text_mountain))
        networkFeeView.setLabelTextColor(getColor(R.color.text_mountain))
    }

    override fun getResult(): Any? = null
}
