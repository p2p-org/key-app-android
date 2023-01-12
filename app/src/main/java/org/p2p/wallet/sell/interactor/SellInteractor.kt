package org.p2p.wallet.sell.interactor

import org.p2p.core.token.Token
import org.p2p.core.utils.isNotZero
import org.p2p.wallet.common.feature_toggles.toggles.remote.SellEnabledFeatureToggle
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
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber
import java.math.BigDecimal

private const val TAG = "SellInteractor"

class SellInteractor(
    private val sellRepository: SellRepository,
    private val currencyRepository: MoonpayCurrenciesRepository,
    private val sellEnabledFeatureToggle: SellEnabledFeatureToggle,
    private val homeLocalRepository: HomeLocalRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val hiddenSellTransactionsStorage: HiddenSellTransactionsStorageContract
) {

    suspend fun loadSellAvailability() {
        if (sellEnabledFeatureToggle.isFeatureEnabled) {
            sellRepository.loadMoonpayFlags()
        }
    }

    suspend fun isSellAvailable(): Boolean {
        return true
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
        val solToken = homeLocalRepository.getUserTokens().find(Token.Active::isSOL)
        requireNotNull(solToken) { "SOL token is not found for current user, can't sell" }

        return sellRepository.getSellQuoteForToken(solToken, solAmount, fiat)
    }

    suspend fun getSolCurrency(): MoonpayCurrency = currencyRepository.getAllCurrencies().first(MoonpayCurrency::isSol)

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
