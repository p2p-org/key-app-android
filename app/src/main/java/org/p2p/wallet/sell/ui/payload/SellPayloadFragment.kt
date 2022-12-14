package org.p2p.wallet.sell.ui.payload

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSellPayloadBinding
import org.p2p.wallet.sell.ui.lock.SellLockedFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import java.math.BigDecimal

class SellPayloadFragment :
    BaseMvpFragment<SellPayloadContract.View, SellPayloadContract.Presenter>(
        R.layout.fragment_sell_payload
    ),
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
            presenter.load()
        }
    }

    override fun showLoading(isVisible: Boolean) {
        binding.shimmerView.isVisible = isVisible
    }

    override fun navigateToSellLock() {
        replaceFragment(SellLockedFragment.create())
    }

    override fun showAvailableSolToSell(totalAmount: BigDecimal) {
        binding.textViewSellAmount.text = getString(R.string.sell_all_sol, totalAmount)
    }

    override fun setMinSolToSell(minAmount: BigDecimal, tokenSymbol: String) {
        binding.editTextInputToken.setHint(tokenSymbol)
        binding.editTextInputToken.setText(minAmount.toString())
    }
}
