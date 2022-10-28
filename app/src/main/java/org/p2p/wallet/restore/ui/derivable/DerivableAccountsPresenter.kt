package org.p2p.wallet.restore.ui.derivable

import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.wallet.auth.analytics.OnboardingAnalytics
import org.p2p.wallet.auth.analytics.OnboardingAnalytics.UsernameRestoreMethod
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.restore.interactor.SeedPhraseInteractor
import org.p2p.wallet.restore.model.DerivableAccount
import timber.log.Timber
import kotlin.properties.Delegates
import kotlinx.coroutines.launch

class DerivableAccountsPresenter(
    private val secretKeys: List<String>,
    private val seedPhraseInteractor: SeedPhraseInteractor,
    private val analytics: OnboardingAnalytics
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

        launch {
            try {
                val accounts = seedPhraseInteractor.getDerivableAccounts(secretKeys)
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
                seedPhraseInteractor.createAndSaveAccount(path, secretKeys)
                analytics.logWalletRestored(ScreenNames.OnBoarding.IMPORT_MANUAL)
                analytics.setUserRestoreMethod(UsernameRestoreMethod.SEED_PHRASE)
                view?.navigateToCreatePin()
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
