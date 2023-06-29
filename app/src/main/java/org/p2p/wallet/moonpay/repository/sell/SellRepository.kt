package org.p2p.wallet.moonpay.repository.sell

import org.p2p.core.token.Token
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpaySellTokenQuote
import org.p2p.wallet.moonpay.model.MoonpaySellError
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.core.crypto.Base58String
import java.math.BigDecimal

interface SellRepository {
    fun isSellAllowedForUser(): Boolean

    suspend fun loadMoonpayFlags()

    @Throws(MoonpaySellError::class)
    suspend fun getUserSellTransactions(
        userAddress: Base58String
    ): List<SellTransaction>

    @Throws(MoonpaySellError::class)
    suspend fun getSellQuoteForToken(
        tokenToSell: Token.Active,
        tokenAmount: BigDecimal,
        fiat: SellTransactionFiatCurrency
    ): MoonpaySellTokenQuote

    suspend fun getSellFiatCurrency(): SellTransactionFiatCurrency

    suspend fun cancelSellTransaction(transactionId: String): MoonpaySellCancelResult
}
