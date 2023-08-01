package org.p2p.token.service.repository.configurator

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.core.token.TokenExtensions
import org.p2p.core.utils.divideSafe

private const val RULE_BY_COUNT_OF_TOKENS = "byCountOfTokensValue"

class PricePercentDifferenceToShow(
    private val extensions: TokenExtensions,
    private val token: Token.Active
) : TokenConfigurator<Token.Active> {

    override fun config(): Token.Active {
        if (extensions.ruleOfProcessingTokenPrice != RULE_BY_COUNT_OF_TOKENS) {
            return token
        }
        // We assume that this configuration will work only with stable coins
        val percentDifferenceToShow = extensions.tokenPercentDifferenceOnWalletScreen

        if (percentDifferenceToShow == null || percentDifferenceToShow.toBigDecimal() == BigDecimal.ZERO) {
            return token
        }
        val acceptableRateDiff = percentDifferenceToShow.toBigDecimal()
        if (isStableCoinRateDiffAcceptable(acceptableRateDiff)) {
            return token
        }
        return token.copy(totalInUsd = token.total, rate = BigDecimal.ONE)
    }

    private fun isStableCoinRateDiffAcceptable(acceptableRateDiff: BigDecimal): Boolean {
        val total = token.total
        val rate = token.rate ?: BigDecimal.ONE
        val fiat = total * rate
        val delta = BigDecimal(100) - ((total.divideSafe(fiat)) * BigDecimal(100))
        return delta.abs() > acceptableRateDiff
    }
}
