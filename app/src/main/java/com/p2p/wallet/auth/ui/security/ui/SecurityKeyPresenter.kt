package com.p2p.wallet.auth.ui.security.ui

import com.p2p.wallet.auth.ui.security.interactor.SecurityKeyInteractor
import com.p2p.wallet.common.mvp.BasePresenter
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class SecurityKeyPresenter(
    private val interactor: SecurityKeyInteractor
) : BasePresenter<SecurityKeyContract.View>(), SecurityKeyContract.Presenter {

    private var keys: List<String> by Delegates.observable(emptyList()) { _, oldValue, newValue ->
        if (newValue != oldValue) {
            view?.showKeys(newValue)
        }
    }

    override fun loadKeys() {
        launch {
            keys = interactor.generateKeys()
        }
    }

    override fun copyKeys() {
        view?.copyToClipboard(keys)
    }
}