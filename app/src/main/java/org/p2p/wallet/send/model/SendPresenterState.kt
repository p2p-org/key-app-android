package org.p2p.wallet.send.model

import org.p2p.core.model.CurrencyMode
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.SOL_SYMBOL
import java.math.BigDecimal
import java.math.BigInteger

class SendPresenterState(
    var inputAmount: String = "0",
    var solToken: Token.Active? = null,
    var mode: CurrencyMode = CurrencyMode.Token(SOL_SYMBOL, 0),
    var sendFeeRelayerFee: SendSolanaFee? = null,
    var searchResult: SearchResult? = null,
    var initialToken: Token.Active? = null,
    var tokenAmount: BigDecimal = BigDecimal.ZERO,
    var usdAmount: BigDecimal = BigDecimal.ZERO,
    var minRentExemption: BigInteger = BigInteger.ZERO
) {

    fun updateInitialToken(initialToken: Token.Active) {
        this.initialToken = initialToken
        mode = CurrencyMode.Token(initialToken.tokenSymbol, initialToken.decimals)
    }
}
