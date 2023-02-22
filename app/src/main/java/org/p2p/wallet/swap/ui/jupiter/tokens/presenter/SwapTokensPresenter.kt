package org.p2p.wallet.swap.ui.jupiter.tokens.presenter

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.swap.ui.jupiter.tokens.SwapTokensContract
import org.p2p.wallet.utils.Base58String

class SwapTokensPresenter(
    private val tokenToChange: SwapTokensChangeToken,
    private val mapper: SwapTokensMapper
) : BasePresenter<SwapTokensContract.View>(), SwapTokensContract.Presenter {

    override fun attach(view: SwapTokensContract.View) {
        super.attach(view)
        view.setTokenItems(
            mapper.toCellItems(Base58String(""), emptyList(), emptyList())
        )
    }

    override fun onSearchTokenQueryChanged(newQuery: String) {
    }

    override fun onTokenClicked() {
    }
}
