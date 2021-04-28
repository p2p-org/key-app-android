package com.p2p.wallet.main.ui.receive

import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class ReceivePresenter(
    private val tokenKeyProvider: TokenKeyProvider
) : BasePresenter<ReceiveContract.View>(), ReceiveContract.Presenter {

    override fun loadData() {
        val key = tokenKeyProvider.publicKey
        view?.showAddress(key)
    }
}