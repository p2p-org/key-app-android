package org.p2p.wallet.history.ui.new_history

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.history.interactor.HistoryServiceInteractor
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import timber.log.Timber

class NewHistoryPresenter(
    private val historyInteractor: HistoryServiceInteractor,
    private val userInteractor: TokenKeyProvider
) : BasePresenter<NewHistoryContract.View>(),
    NewHistoryContract.Presenter {

    override fun loadHistory() {
        launch {
            try {
                historyInteractor.loadHistory(userInteractor.publicKey, userInteractor.keyPair, 10, 0)
            } catch (e: Throwable) {
                Timber.tag("______").d(e)
            }
        }
    }
}
