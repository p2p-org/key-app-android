package org.p2p.wallet.sell.ui.payload

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSellPayloadBinding
import org.p2p.wallet.sell.ui.lock.SellLockContract
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class SellPayloadFragment :
    BaseMvpFragment<SellPayloadContract.View, SellPayloadContract.Presenter>(
        R.layout.fragment_sell_payload
    ),
    SellLockContract.View {

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
}
