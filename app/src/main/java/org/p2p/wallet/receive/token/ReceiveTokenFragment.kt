package org.p2p.wallet.receive.token

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentReceiveTokenBinding
import org.p2p.wallet.utils.viewbinding.viewBinding

class ReceiveTokenFragment :
    BaseMvpFragment<ReceiveTokenContract.View, ReceiveTokenContract.Presenter>(R.layout.fragment_receive_token) {
    override val presenter: ReceiveTokenContract.Presenter by inject()

    companion object {
        fun create() = ReceiveTokenFragment()
    }

    private val binding: FragmentReceiveTokenBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
        }
    }
}