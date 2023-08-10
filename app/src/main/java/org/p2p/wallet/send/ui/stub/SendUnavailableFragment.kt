package org.p2p.wallet.send.ui.stub

import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.token.Token
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentSendUnavailableBinding
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment
import org.p2p.wallet.receive.ReceiveFragmentFactory
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_DEFAULT_TOKEN = "ARG_DEFAULT_TOKEN"

class SendUnavailableFragment : BaseFragment(R.layout.fragment_send_unavailable) {

    companion object {
        fun create(token: Token): Fragment = SendUnavailableFragment().withArgs(
            ARG_DEFAULT_TOKEN to token
        )
    }

    private val binding: FragmentSendUnavailableBinding by viewBinding()
    private val defaultTokenToBuy: Token by args(ARG_DEFAULT_TOKEN)
    private val receiveFragmentFactory: ReceiveFragmentFactory by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }
            buttonBuy.setOnClickListener {
                replaceFragment(NewBuyFragment.create(token = defaultTokenToBuy))
            }
            buttonReceive.setOnClickListener {
                replaceFragment(receiveFragmentFactory.receiveFragment(token = null))
            }
        }
    }

    override fun applyWindowInsets(rootView: View) {
        binding.containerBottom.doOnApplyWindowInsets { view, insets, initialPadding ->
            val systemAndIme = insets.systemAndIme()
            binding.toolbar.updatePadding(top = systemAndIme.top)
            view.updatePadding(bottom = initialPadding.bottom + systemAndIme.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }
}
