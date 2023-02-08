package org.p2p.wallet.sell.interactor

import androidx.core.content.edit
import android.content.SharedPreferences
import org.p2p.core.token.Token
import org.p2p.core.utils.isNotZero
import org.p2p.wallet.common.feature_toggles.toggles.remote.SellEnabledFeatureToggle
import org.p2p.wallet.datastore.preferences.UserPreferencesStore
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.sell.HiddenSellTransactionsStorageContract
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayCurrency
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpaySellTokenQuote
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.wallet.moonpay.repository.currencies.MoonpayCurrenciesRepository
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellCancelResult
import org.p2p.wallet.moonpay.repository.sell.SellRepository
import org.p2p.wallet.moonpay.repository.sell.SellTransactionFiatCurrency
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber
import java.math.BigDecimal

private const val TAG = "SellInteractor"
private const val SHOULD_SHOW_SELL_INFORM_DIALOG_KEY = "SHOULD_SHOW_SELL_INFORM_DIALOG_KEY"

class SellInteractor(
    private val sellRepository: SellRepository,
    private val currencyRepository: MoonpayCurrenciesRepository,
    private val sellEnabledFeatureToggle: SellEnabledFeatureToggle,
    private val homeLocalRepository: HomeLocalRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val userInteractor: UserInteractor,
    private val hiddenSellTransactionsStorage: HiddenSellTransactionsStorageContract,
    private val sharedPreferences: UserPreferencesStore,
) {

    suspend fun shouldShowInformDialog(): Boolean =
        sharedPreferences.getBoolean(SHOULD_SHOW_SELL_INFORM_DIALOG_KEY)

    suspend fun setShouldShowInformDialog(shouldShowAgain: Boolean) {
        sharedPreferences.putBoolean(SHOULD_SHOW_SELL_INFORM_DIALOG_KEY, shouldShowAgain)
    }

    suspend fun loadSellAvailability() {
        if (sellEnabledFeatureToggle.isFeatureEnabled) {
            sellRepository.loadMoonpayFlags()
        } else {
            Timber.tag(TAG).i("Moonpay flags will not fetch, feature toggle is disabled")
        }
    }

    suspend fun isSellAvailable(): Boolean {
        return sellEnabledFeatureToggle.isFeatureEnabled &&
            isUserBalancePositive() &&
            sellRepository.isSellAllowedForUser()
    }

    private suspend fun isUserBalancePositive(): Boolean {
        return try {
            homeLocalRepository.getUserBalance().isNotZero()
        } catch (error: Throwable) {
            Timber.tag(TAG).i(error, "Cant get user balance")
            false
        }
    }

    suspend fun loadUserSellTransactions(): List<SellTransaction> {
        return sellRepository.getUserSellTransactions(tokenKeyProvider.publicKey.toBase58Instance())
    }

    suspend fun getSellQuoteForSol(solAmount: BigDecimal, fiat: SellTransactionFiatCurrency): MoonpaySellTokenQuote {
        val solToken = requireNotNull(userInteractor.getUserSolToken()) {
            "SOL token is not found for current user, can't sell"
        }

        return sellRepository.getSellQuoteForToken(solToken, solAmount, fiat)
    }

    suspend fun getTokenForSell(): Token.Active =
        requireNotNull(userInteractor.getUserSolToken()) {
            "SOL token is not found for current user, can't sell"
        }

    suspend fun getSolCurrency(): MoonpayCurrency {
        return currencyRepository.getAllCurrencies().first(MoonpayCurrency::isSol)
    }

    suspend fun getMoonpaySellFiatCurrency(): SellTransactionFiatCurrency {
        return sellRepository.getSellFiatCurrency()
    }

    suspend fun cancelTransaction(transactionId: String): MoonpaySellCancelResult {
        return sellRepository.cancelSellTransaction(transactionId)
    }

    fun hideTransactionFromHistory(transactionId: String) {
        hiddenSellTransactionsStorage.putTransaction(transactionId)
    }
}
