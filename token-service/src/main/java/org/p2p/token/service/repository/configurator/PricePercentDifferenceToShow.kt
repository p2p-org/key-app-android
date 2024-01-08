package org.p2p.token.service.repository.configurator

import java.math.BigDecimal
import org.p2p.core.token.TokenExtensions
import org.p2p.core.token.TokenMetadataExtension
import org.p2p.core.utils.divideSafe

private const val RULE_BY_COUNT_OF_TOKENS = "byCountOfTokensValue"

class PricePercentDifferenceToShow(
    private val extensions: TokenMetadataExtension,
    private val tokenExtensions: TokenExtensions,
    private val tokenTotal: BigDecimal,
    private val tokenRate: BigDecimal?
) : TokenConfigurator {

    override fun config(): TokenExtensions {
        if (extensions.ruleOfProcessingTokenPriceWs != RULE_BY_COUNT_OF_TOKENS) {
            return tokenExtensions
        }
        // We assume that this configuration will work only with stable coins
        val percentDifferenceToShow = extensions.percentDifferenceToShowByPriceOnWs
        if (percentDifferenceToShow != null && percentDifferenceToShow.toBigDecimal() != BigDecimal.ZERO) {
            val acceptableRateDiff = percentDifferenceToShow.toBigDecimal()
            tokenExtensions.isRateExceedsTheDifference = isRateExceedsTheDifference(acceptableRateDiff)
        }

        return tokenExtensions
    }

    /**
     * If rate differs more than [acceptableRateDiff] then we are showing it's real rate
     * Otherwise, we should make [tokenTotal] as a [tokenTotalInUsd]
     * */
    private fun isRateExceedsTheDifference(acceptableRateDiff: BigDecimal): Boolean {
        val total = tokenTotal
        val rate = tokenRate ?: BigDecimal.ONE
        val fiat = total * rate
        val delta = BigDecimal(100) - ((total.divideSafe(fiat)) * BigDecimal(100))
        return delta.abs() > acceptableRateDiff
    }
}
