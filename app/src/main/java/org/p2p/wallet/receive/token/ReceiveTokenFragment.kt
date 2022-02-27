package org.p2p.wallet.receive.token

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentReceiveTokenBinding
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_TOKEN = "EXTRA_TOKEN"
class ReceiveTokenFragment :
    BaseMvpFragment<ReceiveTokenContract.View, ReceiveTokenContract.Presenter>(R.layout.fragment_receive_token) {
    override val presenter: ReceiveTokenContract.Presenter by inject()

    companion object {
        fun create(token: Token.Active) = ReceiveTokenFragment().withArgs(
            EXTRA_TOKEN to token
        )
    }

    private val binding: FragmentReceiveTokenBinding by viewBinding()
    private val token: Token.Active by args(EXTRA_TOKEN)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            receiveCardView.showQrLoading(false)
        }
    }
}