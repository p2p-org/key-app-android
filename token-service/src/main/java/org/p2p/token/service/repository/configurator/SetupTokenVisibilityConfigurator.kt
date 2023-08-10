package org.p2p.token.service.repository.configurator

import org.p2p.core.token.TokenExtensions
import org.p2p.core.token.TokenMetadataExtension

class SetupTokenVisibilityConfigurator(
    private val extensions: TokenMetadataExtension,
    private val tokenExtensions: TokenExtensions
) : TokenConfigurator {

    override fun config(): TokenExtensions {
        return tokenExtensions.copy(isTokenVisibleOnWalletScreen = extensions.isPositionOnWs)
    }
}
