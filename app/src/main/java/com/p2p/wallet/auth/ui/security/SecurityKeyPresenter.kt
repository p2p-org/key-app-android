package com.p2p.wallet.auth.ui.security

import com.p2p.wallet.auth.interactor.SecurityKeyInteractor
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.user.interactor.UserInteractor
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class SecurityKeyPresenter(
    private val interactor: SecurityKeyInteractor,
    private val userInteractor: UserInteractor
) : BasePresenter<SecurityKeyContract.View>(), SecurityKeyContract.Presenter {

    private var keys: List<String> by Delegates.observable(emptyList()) { _, oldValue, newValue ->
        if (newValue != oldValue) {
            view?.showKeys(newValue)
        }
    }

    override fun createAccount() {
        launch {
            userInteractor.createAndSaveAccount(keys)
            view?.navigateToCreatePin()
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