package org.p2p.wallet.moonpay.repository.sell

import org.p2p.core.token.Token
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpaySellTokenQuote
import org.p2p.wallet.moonpay.model.MoonpaySellError
import org.p2p.wallet.moonpay.model.MoonpaySellTransaction
import org.p2p.wallet.utils.Base58String
import java.math.BigDecimal

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
        tokenAmount: BigDecimal,
        fiat: MoonpaySellFiatCurrency
    ): MoonpaySellTokenQuote

    suspend fun getCurrentCountryAbbreviation(): String
}
