package com.p2p.wallet.main.ui.receive

import android.os.Bundle
import android.view.View
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.databinding.FragmentReceiveBinding
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.shareText
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject

class ReceiveFragment :
    BaseMvpFragment<ReceiveContract.View, ReceiveContract.Presenter>(R.layout.fragment_receive),
    ReceiveContract.View {

    companion object {
        fun create() = ReceiveFragment()
    }

    override val presenter: ReceiveContract.Presenter by inject()

    private val binding: FragmentReceiveBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            toolbar.setOnMenuItemClickListener {
                if (it.itemId == R.id.itemShare) {
                    requireContext().shareText(addressTextView.text.toString())
                    return@setOnMenuItemClickListener true
                }

                return@setOnMenuItemClickListener false
            }
        }

        presenter.loadData()
    }

    override fun showAddress(address: String) {
        binding.addressTextView.text = address
    }
}