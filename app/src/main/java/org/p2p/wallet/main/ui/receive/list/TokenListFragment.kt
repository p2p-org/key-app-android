package org.p2p.wallet.main.ui.receive.list

import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment

class TokenListFragment :
    BaseMvpFragment<TokenListContract.View, TokenListContract.Presenter>(R.layout.fragment_receive_list) {
    override val presenter: TokenListContract.Presenter by inject()
}