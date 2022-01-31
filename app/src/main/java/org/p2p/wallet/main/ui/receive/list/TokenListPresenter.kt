package org.p2p.wallet.main.ui.receive.list

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.user.model.TokenData
import kotlin.properties.Delegates

class TokenListPresenter(
    private val interactor: UserInteractor
) : BasePresenter<TokenListContract.View>(), TokenListContract.Presenter {

    private var allTokens by Delegates.observable<List<TokenData>>(emptyList()) { _, _, newValue ->
        view?.showItems(newValue)
    }
    private val filter = TokenListFilter(allTokens.toMutableList()) { filteredItems ->
        view?.showItems(filteredItems)
    }

    private var searchResult by Delegates.observable<List<TokenData>>(mutableListOf()) { _, _, newItems ->
        view?.showItems(newItems)
    }

    override fun load() {
        launch {
            view?.showLoading(true)
            allTokens = interactor.getAllTokensData()
            view?.showLoading(false)
        }
    }

    override fun search(text: CharSequence?) {
        filter.update(allTokens)
        filter.filter(text)
    }
}