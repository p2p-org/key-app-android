package org.p2p.wallet.history.ui.new_history

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment

class NewHistoryFragment : BaseMvpFragment<NewHistoryContract.View, NewHistoryContract.Presenter>(
    R.layout.fragment_new_history
) {

    companion object {
        fun create() = NewHistoryFragment()
    }

    override val presenter: NewHistoryContract.Presenter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.loadHistory()
    }
}
