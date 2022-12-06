package org.p2p.wallet.send.ui

import android.os.Bundle
import android.view.View
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentSendNoTokenBinding
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_TOKEN = "EXTRA_TOKEN"

class NoTokenSendFragment : BaseFragment(R.layout.fragment_send_no_token) {

    companion object {
        fun create(token: Token) = NoTokenSendFragment().withArgs(
            EXTRA_TOKEN to token
        )
    }

    private val binding: FragmentSendNoTokenBinding by viewBinding()
    private val token: Token by args(EXTRA_TOKEN)
    override val navBarColor: Int = R.color.bg_night

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }
            buttonBuy.setOnClickListener {
                replaceFragment(NewBuyFragment.create(token))
            }

            buttonReceive.setOnClickListener {
                replaceFragment(ReceiveSolanaFragment.create(null))
            }
        }
    }
}
