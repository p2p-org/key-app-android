package org.p2p.wallet.auth.ui.security

import kotlinx.coroutines.launch
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.restore.interactor.SecretKeyInteractor
import kotlin.properties.Delegates

class SecurityKeyPresenter(
    private val secretKeyInteractor: SecretKeyInteractor
) : BasePresenter<SecurityKeyContract.View>(), SecurityKeyContract.Presenter {

    private var keys: List<String> by Delegates.observable(emptyList()) { _, oldValue, newValue ->
        if (newValue != oldValue) {
            view?.showKeys(newValue)
        }
    }

    init {
        loadKeys()
    }

    override fun attach(view: SecurityKeyContract.View) {
        super.attach(view)
        view.showKeys(keys)
    }

    override fun loadKeys() {
        launch {
            keys = secretKeyInteractor.generateSecretKeys()
        }
    }

    override fun copyKeys() {
        view?.copyToClipboard(keys)
    }

    override fun createAndSaveAccount() {
        launch {
            view?.showLoading(true)
            secretKeyInteractor.createAndSaveAccount(DerivationPath.BIP44CHANGE, keys, lookup = false)
            view?.navigateToReserve()
            view?.showLoading(false)
        }
    }
}