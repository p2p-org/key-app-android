package org.p2p.wallet.debug.settings

import android.os.Bundle
import android.view.View
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentTestSwapBinding
import org.p2p.wallet.utils.viewbinding.viewBinding

class TestSwapFragment : BaseFragment(R.layout.fragment_test_swap) {

    private val binding: FragmentTestSwapBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.getRoute.setOnClickListener {
            val from = binding.fromInput.text
            val to = binding.toInput.text
            val amount = binding.amount.text

            binding.swap.isEnabled = true

            binding.swap.setOnClickListener {
            }
        }
    }
}
