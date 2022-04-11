package org.p2p.wallet.history.ui.history

import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment

class HistoryFragment :
    BaseMvpFragment<HistoryContract.View, HistoryContract.Presenter>(R.layout.fragment_history),
    HistoryContract.View {

    override val presenter: HistoryContract.Presenter by inject()

    companion object {
        fun create() = HistoryFragment()
    }
}
