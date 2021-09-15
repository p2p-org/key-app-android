package com.p2p.wallet.main.ui.receive

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseFragment
import com.p2p.wallet.databinding.FragmentReceiveBinding
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.main.ui.receive.renbtc.RenBTCFragment
import com.p2p.wallet.main.ui.receive.solana.ReceiveSolanaFragment
import com.p2p.wallet.utils.args
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.viewbinding.viewBinding
import com.p2p.wallet.utils.withArgs

private const val EXTRA_TOKEN = "EXTRA_TOKEN"

class ReceiveFragment : BaseFragment(R.layout.fragment_receive) {

    companion object {
        fun create(token: Token?) = ReceiveFragment().withArgs(
            EXTRA_TOKEN to token
        )
    }

    private val binding: FragmentReceiveBinding by viewBinding()

    private val token: Token? by args(EXTRA_TOKEN)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            viewPager.adapter = ReceivePagerAdapter(this@ReceiveFragment)

            tabsRadioGroup.check(R.id.solanaButton)
            tabsRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.solanaButton -> viewPager.currentItem = 0
                    R.id.renBtcButton -> viewPager.currentItem = 1
                }
            }
        }
    }

    inner class ReceivePagerAdapter(fragment: ReceiveFragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> ReceiveSolanaFragment.create(token)
            1 -> RenBTCFragment.create()
            else -> throw IllegalStateException("Unknown position $position for receive fragment")
        }
    }
}