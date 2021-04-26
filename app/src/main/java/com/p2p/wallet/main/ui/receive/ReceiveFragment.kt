package com.p2p.wallet.main.ui.receive

import android.os.Bundle
import android.view.View
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseMvpFragment
import org.koin.android.ext.android.inject

class ReceiveFragment :
    BaseMvpFragment<ReceiveContract.View, ReceiveContract.Presenter>(R.layout.fragment_receive),
    ReceiveContract.View {

    companion object {
        fun create() = ReceiveFragment()
    }

    override val presenter: ReceiveContract.Presenter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}