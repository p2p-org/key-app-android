package org.p2p.token.service.repository.configurator

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.core.token.TokenMetadataExtension
import org.p2p.core.utils.divideSafe
import org.p2p.core.utils.orZero

private const val RULE_BY_COUNT_OF_TOKENS = "byCountOfTokensValue"

class PercentDifferenceToShowByPriceConfigurator(
    private val extensions: TokenMetadataExtension,
    private val token: Token.Active
) : TokenConfigurator<Token.Active> {

    override fun config(): Token.Active {
        if (extensions.ruleOfProcessingTokenPriceWs != RULE_BY_COUNT_OF_TOKENS) {
            return token
        }
        // We assume that this configuration will work only with stable coins
        val percentDifferenceToShow = extensions.percentDifferenceToShowByPriceOnWs

        if (percentDifferenceToShow == null || percentDifferenceToShow == 0) {
            return token
        }
        val acceptableRateDiff = percentDifferenceToShow.toBigDecimal().divideSafe(BigDecimal(100))
        if (isStableCoinRateDiffAcceptable(acceptableRateDiff)) {
            return token
        }
        return token.copy(totalInUsd = token.total, rate = BigDecimal.ONE)
    }

    private fun isStableCoinRateDiffAcceptable(acceptableRateDiff: BigDecimal): Boolean {
        val delta = token.rate.orZero() - BigDecimal.ONE
        return delta.abs() < acceptableRateDiff
    }
}
