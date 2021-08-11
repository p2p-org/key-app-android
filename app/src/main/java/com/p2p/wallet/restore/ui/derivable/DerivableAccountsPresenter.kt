package com.p2p.wallet.restore.ui.derivable

import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.restore.interactor.SecretKeyInteractor
import com.p2p.wallet.restore.model.DerivableAccount
import com.p2p.wallet.restore.model.SecretKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.solanaj.crypto.DerivationPath
import kotlin.properties.Delegates

class DerivableAccountsPresenter(
    private val secretKeys: List<SecretKey>,
    private val secretKeyInteractor: SecretKeyInteractor
) : BasePresenter<DerivableAccountsContract.View>(),
    DerivableAccountsContract.Presenter {

    private var path: DerivationPath by Delegates.observable(DerivationPath.BIP44CHANGE) { _, oldValue, newValue ->
        if (oldValue != newValue) filterAccountsByPath(newValue)
    }

    private val allAccounts = mutableListOf<DerivableAccount>()

    override fun setNewPath(path: DerivationPath) {
        this.path = path
    }

    override fun loadData() {
        view?.showLoading(true)
        val keys = secretKeys.map { it.text }

        launch {
            delay(250L)
            val accounts = secretKeyInteractor.getDerivableAccounts(DerivationPath.BIP44CHANGE, keys)
            allAccounts.addAll(accounts)
            view?.showLoading(false)
            filterAccountsByPath(path)
        }

        launch {
            delay(250L)
            val accounts = secretKeyInteractor.getDerivableAccounts(DerivationPath.BIP44, keys)
            allAccounts.addAll(accounts)
        }

        launch {
            delay(250L)
            val accounts = secretKeyInteractor.getDerivableAccounts(DerivationPath.BIP32DEPRECATED, keys)
            allAccounts.addAll(accounts)
        }
    }

    override fun createAndSaveAccount() {
        launch {
            val keys = secretKeys.map { it.text }
            secretKeyInteractor.createAndSaveAccount(path, keys)
            view?.navigateToCreatePin()
        }
    }

    override fun loadCurrentPath() {
        view?.showPathSelectionDialog(path)
    }

    private fun filterAccountsByPath(path: DerivationPath) {
        launch {
            val accounts = allAccounts.filter { it.path == path }
            view?.showAccounts(path, accounts)
        }
    }
}