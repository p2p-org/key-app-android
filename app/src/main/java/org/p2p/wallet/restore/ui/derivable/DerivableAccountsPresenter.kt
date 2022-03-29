package org.p2p.wallet.restore.ui.derivable

import kotlinx.coroutines.launch
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.wallet.auth.analytics.OnBoardingAnalytics
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.restore.interactor.SecretKeyInteractor
import org.p2p.wallet.restore.model.DerivableAccount
import org.p2p.wallet.restore.model.SecretKey
import timber.log.Timber
import kotlin.properties.Delegates

class DerivableAccountsPresenter(
    private val secretKeys: List<SecretKey>,
    private val secretKeyInteractor: SecretKeyInteractor,
    private val usernameInteractor: UsernameInteractor,
    private val analytics: OnBoardingAnalytics
) : BasePresenter<DerivableAccountsContract.View>(),
    DerivableAccountsContract.Presenter {

    private var path: DerivationPath by Delegates.observable(DerivationPath.BIP44CHANGE) { _, _, newValue ->
        filterAccountsByPath(newValue)
    }

    private val allAccounts = mutableListOf<DerivableAccount>()

    override fun setNewPath(path: DerivationPath) {
        this.path = path
    }

    override fun loadData() {
        if (allAccounts.isNotEmpty()) return

        view?.showLoading(true)
        val keys = secretKeys.map { it.text }

        launch {
            try {
                val accounts = secretKeyInteractor.getDerivableAccounts(keys)
                allAccounts += accounts
                filterAccountsByPath(path)
                if (allAccounts.size > 1) {
                    analytics.logManyWalletFound(ScreenNames.OnBoarding.IMPORT_MANUAL)
                } else {
                    analytics.logNoWalletFound(ScreenNames.OnBoarding.IMPORT_MANUAL)
                }
            } catch (e: Throwable) {
                Timber.e(e, "Error loading derivable accounts")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun createAndSaveAccount() {
        launch {
            try {
                view?.showLoading(true)
                val keys = secretKeys.map { it.text }
                secretKeyInteractor.createAndSaveAccount(path, keys)
                analytics.logWalletRestored(ScreenNames.OnBoarding.IMPORT_MANUAL)
                val usernameExists = usernameInteractor.usernameExists()
                if (usernameExists) {
                    view?.navigateToCreatePin()
                } else {
                    view?.navigateToReserveUsername()
                }
            } catch (e: Throwable) {
                Timber.e(e, "Error while creating account and checking username")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private fun filterAccountsByPath(path: DerivationPath) {
        launch {
            val accounts = allAccounts.filter { it.path == path }
            view?.showAccounts(accounts)
        }
    }
}
