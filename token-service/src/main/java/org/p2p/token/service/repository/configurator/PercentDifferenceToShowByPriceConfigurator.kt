package org.p2p.token.service.repository.configurator

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.core.token.TokenMetadataExtension

private const val RULE_BY_COUNT_OF_TOKENS = "byCountOfTokensValue"

class PercentDifferenceToShowByPriceConfigurator(
    private val extensions: TokenMetadataExtension,
    private val token: Token.Active
) : TokenConfigurator<Token.Active> {

    override fun config(): Token.Active {
        val ruleOfProcessing = extensions.ruleOfProcessingTokenPriceWs

        // 1. Select token fiat
        val tokenFiatValue = token.total
        // 2. Select token rate
        val tokenRate = token.rate ?: BigDecimal.ONE
        // 3. Calculate token value in USD
        val tokenTotalInUsd = tokenRate * tokenFiatValue
        // 4. Calculate C value
        val c = BigDecimal(100) - (tokenFiatValue.divide(tokenTotalInUsd) * BigDecimal(100))
        // 5. find take percent difference to show by price on wallet screen
        val percentDifferenceToShowByPrice = BigDecimal(extensions.percentDifferenceToShowByPriceOnWs ?: 0)

        // 5. if C value less that [percentDifferenceToShowPrice] and ruleOfProcessing == byCountOfTokensValue
        if (c < percentDifferenceToShowByPrice && ruleOfProcessing == RULE_BY_COUNT_OF_TOKENS) {
            return token.copy(totalInUsd = token.total)
        }
        // 6. Else do nothing with token
        return token
    }
}
