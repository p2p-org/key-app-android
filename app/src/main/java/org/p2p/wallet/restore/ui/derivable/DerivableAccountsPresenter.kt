package org.p2p.wallet.restore.ui.derivable

import timber.log.Timber
import kotlin.properties.Delegates
import kotlinx.coroutines.launch
import org.p2p.core.utils.Constants.SOL_COINGECKO_ID
import org.p2p.core.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.wallet.auth.analytics.OnboardingAnalytics
import org.p2p.wallet.auth.analytics.RestoreWalletAnalytics
import org.p2p.wallet.auth.analytics.RestoreWalletAnalytics.UsernameRestoreMethod
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.restore.interactor.SeedPhraseInteractor
import org.p2p.wallet.restore.model.DerivableAccount
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.user.repository.prices.TokenId
import org.p2p.wallet.user.repository.prices.impl.TokenPricesCoinGeckoRepository

class DerivableAccountsPresenter(
    private val secretKeys: List<String>,
    private val seedPhraseInteractor: SeedPhraseInteractor,
    private val analytics: OnboardingAnalytics,
    private val restoreWalletAnalytics: RestoreWalletAnalytics,
    private val userLocalRepository: UserLocalRepository,
    private val tokenPricesCoinGeckoRepository: TokenPricesCoinGeckoRepository
) : BasePresenter<DerivableAccountsContract.View>(),
    DerivableAccountsContract.Presenter {

    private var path: DerivationPath by Delegates.observable(DerivationPath.BIP44CHANGE) { _, _, newValue ->
        filterAccountsByPath(newValue)
    }

    private val allAccounts = mutableListOf<DerivableAccount>()

    private var solPrice: TokenPrice? = null

    override fun attach(view: DerivableAccountsContract.View) {
        super.attach(view)
        loadSolRate()
    }

    override fun setNewPath(path: DerivationPath) {
        this.path = path
    }

    override fun loadData() {
        if (allAccounts.isNotEmpty()) return

        view?.showLoading(true)

        launch {
            try {
                val accounts = seedPhraseInteractor.getDerivableAccounts(secretKeys)

                val updatedAccounts = if (solPrice != null) {
                    accounts.map { it.copy(totalInUsd = solPrice!!.price) }
                } else {
                    accounts
                }

                allAccounts += updatedAccounts
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
                restoreWalletAnalytics.setUserRestoreMethod(UsernameRestoreMethod.SEED_PHRASE)

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

    private fun loadSolRate() {
        launch {
            try {
                val tokenId = TokenId(SOL_COINGECKO_ID)
                val solRate = tokenPricesCoinGeckoRepository.getTokenPriceById(tokenId, USD_READABLE_SYMBOL).also {
                    solPrice = it
                }

                val updatedAccounts = allAccounts.map { it.copy(totalInUsd = solRate.price) }
                view?.showAccounts(updatedAccounts)
            } catch (e: Throwable) {
                Timber.e(e, "Error loading SOL rate")
            }
        }
    }
}
