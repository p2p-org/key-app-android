package org.p2p.token.service.repository.configurator

import org.p2p.core.token.TokenMetadataExtension
import org.p2p.core.token.TokenExtensions

class SetupTokenCellVisibilityConfigurator(
    private val extensions: TokenMetadataExtension,
    private val tokenExtensions: TokenExtensions
) : TokenConfigurator {

    override fun config(): TokenExtensions {
        return tokenExtensions.copy(isTokenCellVisibleOnWalletScreen = extensions.isTokenCellVisibleOnWs)
    }
}
