package org.p2p.wallet.moonpay.repository.sell

import org.p2p.core.token.Token
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpaySellTokenQuote
import org.p2p.wallet.moonpay.model.MoonpaySellError
import org.p2p.wallet.moonpay.model.MoonpaySellTransaction
import org.p2p.wallet.utils.Base58String

interface MoonpaySellRepository {
    fun isSellAllowedForUser(): Boolean

    suspend fun loadMoonpayFlags()

    @Throws(MoonpaySellError::class)
    suspend fun getUserSellTransactions(
        userAddress: Base58String
    ): List<MoonpaySellTransaction>

    @Throws(MoonpaySellError::class)
    suspend fun getSellQuoteForToken(
        tokenToSell: Token.Active,
        tokenAmount: Double,
        fiat: MoonpaySellFiatCurrency
    ): MoonpaySellTokenQuote

    suspend fun cancelSellTransaction(transactionId: String): MoonpaySellCancelResult
}
