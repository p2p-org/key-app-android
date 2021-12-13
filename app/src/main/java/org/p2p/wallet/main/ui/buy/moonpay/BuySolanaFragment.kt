package org.p2p.wallet.main.ui.buy.moonpay

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.textwatcher.PrefixTextWatcher
import org.p2p.wallet.databinding.FragmentBuySolanaBinding
import org.p2p.wallet.main.model.BuyData
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class BuySolanaFragment :
    BaseMvpFragment<BuySolanaContract.View, BuySolanaContract.Presenter>(R.layout.fragment_buy_solana),
    BuySolanaContract.View {

    companion object {
        fun create() = BuySolanaFragment()
    }

    override val presenter: BuySolanaContract.Presenter by inject()

    private val binding: FragmentBuySolanaBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            PrefixTextWatcher.installOn(payEditText) {
                purchaseCostView.setValueText(it)
                continueButton.isEnabled = it.isNotEmpty()
                presenter.setBuyAmount(it)
            }
        }

        presenter.loadSolData()
    }

    override fun showData(data: BuyData) {
        with(binding) {
            priceView.setValueText(data.priceText)
            processingFeeView.setValueText(data.processingFeeText)
            networkFeeView.setValueText(data.networkFeeText)
            accountCreationView.setValueText(data.accountCreationCostText)

            totalView.setValueText(data.total)
        }
    }
}