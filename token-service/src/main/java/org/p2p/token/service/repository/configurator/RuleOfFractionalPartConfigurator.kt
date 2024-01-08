package org.p2p.token.service.repository.configurator

import org.p2p.core.token.TokenExtensions
import org.p2p.core.token.TokenMetadataExtension

private const val RULE_DROP_AFTER_HUNDREDTH_PART = "droppingAfterHundredthPart"

class RuleOfFractionalPartConfigurator(
    private val extensions: TokenMetadataExtension,
    private val tokenExtensions: TokenExtensions
) : TokenConfigurator {

    override fun config(): TokenExtensions {
        val ruleOfFractionalPart = extensions.ruleOfFractionalPartOnWs
        var numbersAfterDecimalPoint: Int? = null
        if (ruleOfFractionalPart == RULE_DROP_AFTER_HUNDREDTH_PART) {
            numbersAfterDecimalPoint = 2
        }
        return tokenExtensions.copy(numbersAfterDecimalPoint = numbersAfterDecimalPoint)
    }
}
