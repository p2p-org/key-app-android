package com.p2p.wowlet.auth.ui.securitykey.ui

import com.p2p.wowlet.auth.ui.securitykey.interactor.SecurityKeyInteractor
import com.p2p.wowlet.common.mvp.BasePresenter
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class SecurityKeyPresenter(
    private val interactor: SecurityKeyInteractor
) : BasePresenter<SecurityKeyContract.View>(), SecurityKeyContract.Presenter {

    private var phrases: List<String> by Delegates.observable(emptyList()) { _, oldValue, newValue ->
        if (newValue != oldValue) {
            view?.showPhrases(newValue)
        }
    }

    override fun loadPhrases() {
        launch {
            phrases = interactor.generatePhrase()
        }
    }

    override fun copyPhrases() {
        view?.copyToClipboard(phrases)
    }
}