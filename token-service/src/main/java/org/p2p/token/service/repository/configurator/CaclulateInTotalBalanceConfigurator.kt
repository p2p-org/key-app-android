package org.p2p.token.service.repository.configurator

import org.p2p.core.token.MetadataExtension
import org.p2p.core.token.Token
import org.p2p.core.token.TokenExtensions

class CaclulateInTotalBalanceConfigurator(
    private val extensions: MetadataExtension,
    private val tokenExtensions: TokenExtensions
) : TokenConfigurator {

    override fun config(token: Token.Active): TokenExtensions {
        return tokenExtensions.copy(isCalculateWithTotalBalance = extensions.calculationOfFinalBalanceOnWs ?: true)
    }
}
