package org.p2p.wallet.sell.interactor

import androidx.core.content.edit
import android.content.SharedPreferences
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import org.p2p.core.token.Token
import org.p2p.core.utils.isNotZero
import org.p2p.wallet.common.feature_toggles.toggles.remote.SellEnabledFeatureToggle
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.sell.HiddenSellTransactionsStorageContract
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayCurrency
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpaySellTokenQuote
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.wallet.moonpay.repository.currencies.MoonpayCurrenciesRepository
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellCancelResult
import org.p2p.wallet.moonpay.repository.sell.SellRepository
import org.p2p.wallet.moonpay.repository.sell.SellTransactionFiatCurrency
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.utils.Constants
import org.p2p.wallet.user.interactor.UserTokensInteractor

private const val TAG = "SellInteractor"
private const val SHOULD_SHOW_SELL_INFORM_DIALOG_KEY = "SHOULD_SHOW_SELL_INFORM_DIALOG_KEY"

class SellInteractor(
    private val sellRepository: SellRepository,
    private val currencyRepository: MoonpayCurrenciesRepository,
    private val userTokensInteractor: UserTokensInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val sellEnabledFeatureToggle: SellEnabledFeatureToggle,
    private val hiddenSellTransactionsStorage: HiddenSellTransactionsStorageContract,
    private val sharedPreferences: SharedPreferences,
) {

    fun shouldShowInformDialog(): Boolean =
        sharedPreferences.getBoolean(SHOULD_SHOW_SELL_INFORM_DIALOG_KEY, true)

    fun setShouldShowInformDialog(shouldShowAgain: Boolean) {
        sharedPreferences.edit { putBoolean(SHOULD_SHOW_SELL_INFORM_DIALOG_KEY, shouldShowAgain) }
    }

    suspend fun loadSellAvailability() {
        Timber.i("Loading loadSellAvailability")
        sellRepository.loadMoonpayFlags()
    }

    suspend fun isSellAvailable(): Boolean {
        val isSellAvailable = sellEnabledFeatureToggle.isFeatureEnabled &&
            isUserBalancePositive() &&
            sellRepository.isSellAllowedForUser()

        val debugInfo = buildString {
            append("isFeatureEnabled=${sellEnabledFeatureToggle.isFeatureEnabled} ")
            append("isUserBalancePositive=${isUserBalancePositive()} ")
            append("isSellAllowedForUser=${sellRepository.isSellAllowedForUser()} ")
        }
        Timber.i("Checking if sell is available: $debugInfo")
        return isSellAvailable
    }

    private suspend fun isUserBalancePositive(): Boolean {
        return try {
            userTokensInteractor.getUserTokens().any { it.total.isNotZero() }
        } catch (error: Throwable) {
            Timber.tag(TAG).e(error, "Cant get user balance")
            false
        }
    }

    suspend fun loadUserSellTransactions(): List<SellTransaction> {
        return sellRepository.getUserSellTransactions(tokenKeyProvider.publicKey.toBase58Instance())
    }

    suspend fun getSellQuoteForSol(solAmount: BigDecimal, fiat: SellTransactionFiatCurrency): MoonpaySellTokenQuote {
        val solToken = requireNotNull(userTokensInteractor.getUserSolToken()) {
            "SOL token is not found for current user, can't sell"
        }

        return sellRepository.getSellQuoteForToken(solToken, solAmount, fiat)
    }

    fun observeTokenForSell(): Flow<Token.Active> =
        userTokensInteractor.observeUserToken(Constants.WRAPPED_SOL_MINT.toBase58Instance())

    suspend fun getSolCurrency(): MoonpayCurrency {
        return currencyRepository.getAllCurrencies().first(MoonpayCurrency::isSol)
    }

    suspend fun getMoonpaySellFiatCurrency(): SellTransactionFiatCurrency {
        return sellRepository.getSellFiatCurrency()
    }

    suspend fun cancelTransaction(transactionId: String): MoonpaySellCancelResult {
        Timber.i("Cancelling sell transaction")
        return sellRepository.cancelSellTransaction(transactionId)
    }

    fun hideTransactionFromHistory(transactionId: String) {
        hiddenSellTransactionsStorage.putTransaction(transactionId)
    }
}
