package org.p2p.wallet.moonpay.repository.sell

import java.math.BigDecimal
import org.p2p.core.crypto.Base58String
import org.p2p.core.token.Token
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpaySellTokenQuote
import org.p2p.wallet.moonpay.model.MoonpaySellError
import org.p2p.wallet.moonpay.model.SellTransaction

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
        fiat: FiatCurrency
    ): MoonpaySellTokenQuote

    suspend fun getSellFiatCurrency(): FiatCurrency

    suspend fun cancelSellTransaction(transactionId: String): MoonpaySellCancelResult
}
