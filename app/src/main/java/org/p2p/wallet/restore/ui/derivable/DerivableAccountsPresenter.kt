package org.p2p.wallet.restore.ui.derivable

import timber.log.Timber
import kotlin.properties.Delegates.observable
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
import org.p2p.wallet.user.repository.prices.TokenId
import org.p2p.wallet.user.repository.prices.impl.TokenPricesCoinGeckoRepository

class DerivableAccountsPresenter(
    private val secretKeys: List<String>,
    private val seedPhraseInteractor: SeedPhraseInteractor,
    private val analytics: OnboardingAnalytics,
    private val restoreWalletAnalytics: RestoreWalletAnalytics,
    private val tokenPricesCoinGeckoRepository: TokenPricesCoinGeckoRepository
) : BasePresenter<DerivableAccountsContract.View>(),
    DerivableAccountsContract.Presenter {

    private var path: DerivationPath by observable(DerivationPath.BIP44CHANGE) { _, _, newValue ->
        filterAccountsByPathAndShow(newValue)
    }

    private var allAccounts = mutableListOf<DerivableAccount>()

    private var solRate: TokenPrice? = null

    override fun attach(view: DerivableAccountsContract.View) {
        super.attach(view)
        loadSolRate()
    }

    private fun loadSolRate() {
        launch {
            try {
                val tokenId = TokenId(SOL_COINGECKO_ID)
                val solRate = tokenPricesCoinGeckoRepository.getTokenPriceById(tokenId, USD_READABLE_SYMBOL)
                    .also { solRate = it }

                allAccounts = allAccounts.updateWithTotalInUsd(solRate).toMutableList()
                filterAccountsByPathAndShow(path)
            } catch (e: Throwable) {
                Timber.e(e, "Error loading SOL rate")
            }
        }
    }

    override fun setNewPath(path: DerivationPath) {
        this.path = path
    }

    override fun loadData() {
        if (allAccounts.isNotEmpty()) return

        view?.showLoading(isLoading = true)

        launch {
            try {
                val accounts = seedPhraseInteractor.getDerivableAccounts(secretKeys)

                val updatedWithRateAccounts = solRate?.let {
                    accounts.updateWithTotalInUsd(it)
                } ?: accounts

                allAccounts += updatedWithRateAccounts
                filterAccountsByPathAndShow(path)

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

    private fun List<DerivableAccount>.updateWithTotalInUsd(solRate: TokenPrice): List<DerivableAccount> {
        return map {
            val totalInUsd = it.totalInSol.multiply(solRate.price)
            it.copy(totalInUsd = totalInUsd)
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

    private fun filterAccountsByPathAndShow(path: DerivationPath) {
        view?.showAccounts(allAccounts.filter { it.path == path })
    }
}
