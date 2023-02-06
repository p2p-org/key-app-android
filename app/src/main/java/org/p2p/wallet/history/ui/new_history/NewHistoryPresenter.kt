package org.p2p.wallet.history.ui.new_history

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.history.interactor.HistoryServiceInteractor

class NewHistoryPresenter(
    private val historyInteractor: HistoryServiceInteractor,
) : BasePresenter<NewHistoryContract.View>(),
    NewHistoryContract.Presenter {

    private val limitPage = 10
    private val offset = 0

    override fun loadHistory() {
        launch {
            try {
                historyInteractor.loadHistory(limitPage, offset)
            } catch (e: Throwable) { }
        }
    }
}
