package org.p2p.token.service.repository.configurator

import org.p2p.core.token.MetadataExtension
import org.p2p.core.token.Token
import org.p2p.core.token.TokenExtensions

class SetupTokenCellVisibilityConfigurator(
    private val extensions: MetadataExtension,
    private val tokenExtensions: TokenExtensions
) : TokenConfigurator {

    override fun config(token: Token.Active): TokenExtensions {
        token.copy(totalInUsd = token.total)
        return tokenExtensions.copy(isTokenCellVisibleOnWalletScreen = extensions.isTokenCellVisibleOnWs ?: true)
    }
}
