package org.p2p.wallet.auth.ui.security

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.restore.interactor.SecretKeyInteractor
import kotlinx.coroutines.launch
import org.p2p.solanaj.crypto.DerivationPath
import kotlin.properties.Delegates

class SecurityKeyPresenter(
    private val interactor: SecretKeyInteractor
) : BasePresenter<SecurityKeyContract.View>(), SecurityKeyContract.Presenter {

    private var keys: List<String> by Delegates.observable(emptyList()) { _, oldValue, newValue ->
        if (newValue != oldValue) {
            view?.showKeys(newValue)
        }
    }

    override fun createAccount() {
        launch {
            /* Creating account in default bip44change path */
            interactor.createAndSaveAccount(DerivationPath.BIP44CHANGE, keys)
            view?.navigateToCreatePin()
        }
    }

    override fun loadKeys() {
        launch {
            keys = interactor.generateSecretKeys()
        }
    }

    override fun copyKeys() {
        view?.copyToClipboard(keys)
    }
}