package com.p2p.wallet.restore.ui.derivable

import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.restore.interactor.SecretKeyInteractor
import com.p2p.wallet.restore.model.DerivableAccount
import com.p2p.wallet.restore.model.SecretKey
import kotlinx.coroutines.async
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
        launch {
            view?.showLoading(true)
            delay(250L)
            val keys = secretKeys.map { it.text }
            val bip44Change = async {
                val accounts = secretKeyInteractor.getDerivableAccounts(DerivationPath.BIP44CHANGE, keys)
                allAccounts.addAll(accounts)
            }

            val bip44 = async {
                val accounts = secretKeyInteractor.getDerivableAccounts(DerivationPath.BIP44, keys)
                allAccounts.addAll(accounts)
            }

            val bip32 = async {
                val accounts = secretKeyInteractor.getDerivableAccounts(DerivationPath.BIP32DEPRECATED, keys)
                allAccounts.addAll(accounts)
            }

            bip44Change.await()
            /* Meanwhile accounts in other paths are being downloaded we can show first fetched accounts */
            view?.showLoading(false)
            filterAccountsByPath(path)

            bip44.await()
            bip32.await()
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