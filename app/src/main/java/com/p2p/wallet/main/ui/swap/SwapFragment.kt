package com.p2p.wallet.main.ui.swap

import android.os.Bundle
import android.view.View
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.databinding.FragmentSwapBinding
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject

class SwapFragment :
    BaseMvpFragment<SwapContract.View, SwapContract.Presenter>(R.layout.fragment_swap),
    SwapContract.View {

    companion object {
        fun create() = SwapFragment()
    }

    override val presenter: SwapContract.Presenter by inject()

    private val binding: FragmentSwapBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
        }
    }
}