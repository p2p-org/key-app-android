package org.p2p.wallet.history.ui.history

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.user.interactor.UserInteractor

private const val PAGE_SIZE = 20

class HistoryPresenter(
    private val userInteractor: UserInteractor,
    private val historyInteractor: HistoryInteractor,
) : BasePresenter<HistoryContract.View>(), HistoryContract.Presenter {

    private var token: Token.Active? = null

    override fun attach(view: HistoryContract.View) {
        super.attach(view)
        observeHistory()
    }

    private fun observeHistory() {
    }
}
