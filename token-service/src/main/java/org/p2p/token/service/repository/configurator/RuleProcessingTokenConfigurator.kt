package org.p2p.token.service.repository.configurator

import org.p2p.core.token.TokenExtensions
import org.p2p.core.token.TokenMetadataExtension

class RuleProcessingTokenConfigurator(
    private val extensions: TokenMetadataExtension,
    private val tokenExtensions: TokenExtensions
) : TokenConfigurator {

    override fun config(): TokenExtensions {
        val processingRule = extensions.ruleOfProcessingTokenPriceWs
        return tokenExtensions.copy(ruleOfProcessingTokenPrice = processingRule)
    }
}
