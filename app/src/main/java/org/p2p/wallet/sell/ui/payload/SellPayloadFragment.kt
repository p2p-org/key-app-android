package org.p2p.wallet.sell.ui.payload

import androidx.core.view.isVisible
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSellPayloadBinding
import org.p2p.wallet.sell.ui.lock.SellLockedArguments
import org.p2p.wallet.sell.ui.lock.SellLockedFragment
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.viewbinding.viewBinding
import timber.log.Timber
import java.math.BigDecimal

class SellPayloadFragment :
    BaseMvpFragment<SellPayloadContract.View, SellPayloadContract.Presenter>(R.layout.fragment_sell_payload),
    SellPayloadContract.View {

    companion object {
        fun create() = SellPayloadFragment()
    }

    override val presenter: SellPayloadContract.Presenter by inject()
    private val binding: FragmentSellPayloadBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            buttonSend.setOnClickListener {
                presenter.cashOut()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.load()
    }

    override fun showLoading(isVisible: Boolean) {
        binding.shimmerView.isVisible = isVisible
    }

    override fun navigateToSellLock(
        solAmount: BigDecimal,
        usdAmount: String,
        moonpayAddress: Base58String
    ) {
        val args = SellLockedArguments(
            solAmount = solAmount,
            amountInUsd = usdAmount,
            moonpayAddress = moonpayAddress.base58Value
        )
        replaceFragment(SellLockedFragment.create(args), addToBackStack = false)
    }

    override fun showAvailableSolToSell(totalAmount: BigDecimal) {
        binding.textViewSellAmount.text = getString(R.string.sell_all_sol, totalAmount)
    }

    override fun setMinSolToSell(minAmount: BigDecimal, tokenSymbol: String) {
        binding.editTextInputToken.setHint(tokenSymbol)
        binding.editTextInputToken.setText(minAmount.toString())
    }

    override fun showMoonpayWidget(url: String) {
        Timber.i("Opening Moonpay Sell widget: $url")
        requireContext().showUrlInCustomTabs(url)
    }
}
