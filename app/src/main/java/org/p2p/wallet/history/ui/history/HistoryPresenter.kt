package org.p2p.wallet.history.ui.history

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.infrastructure.network.data.EmptyDataException
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import timber.log.Timber
import kotlin.properties.Delegates

private const val PAGE_SIZE = 20

class HistoryPresenter(
    private val historyInteractor: HistoryInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) : BasePresenter<HistoryContract.View>(), HistoryContract.Presenter {

    private val publicKey = tokenKeyProvider.publicKey

    private var transactions by Delegates.observable<List<HistoryTransaction>>(emptyList()) { _, _, newValue ->
        view?.showHistory(items = newValue)
        view?.showPagingState(PagingState.Idle)
    }

    override fun loadHistory(isRefresh: Boolean) {
        launch {
            if (transactions.isEmpty()) {
                view?.showPagingState(PagingState.InitialLoading)
            } else {
                view?.showPagingState(PagingState.Loading)
            }
            val lastSignature = transactions.lastOrNull()?.signature
            runCatching {
                historyInteractor.getTransactionHistory2(
                    tokenPublicKey = publicKey,
                    forceNetwork = isRefresh,
                    limit = PAGE_SIZE,
                    lastSignature = lastSignature
                )
            }
                .onSuccess(::onSuccess)
                .onFailure(::onFailure)
        }
    }

    private fun onSuccess(items: List<HistoryTransaction>) {
        transactions = transactions + items
    }

    private fun onFailure(e: Throwable) {
        Timber.e("Error getting transaction history")
        if (e is EmptyDataException) {
            transactions = transactions.toMutableList()
        } else {
            view?.showPagingState(PagingState.Error(e))
        }
    }
}
