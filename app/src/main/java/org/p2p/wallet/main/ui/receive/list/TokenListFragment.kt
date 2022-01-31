package org.p2p.wallet.main.ui.receive.list

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentReceiveListBinding
import org.p2p.wallet.utils.viewbinding.viewBinding

class TokenListFragment :
    BaseMvpFragment<TokenListContract.View, TokenListContract.Presenter>(R.layout.fragment_receive_list) {

    companion object {
        fun create() = TokenListFragment()
    }

    override val presenter: TokenListContract.Presenter by inject()
    private val binding: FragmentReceiveListBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {

        }
    }
}