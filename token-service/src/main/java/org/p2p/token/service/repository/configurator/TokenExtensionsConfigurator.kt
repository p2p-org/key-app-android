package org.p2p.token.service.repository.configurator

import org.p2p.core.token.TokenMetadataExtension
import org.p2p.core.token.Token
import org.p2p.core.token.TokenExtensions

class TokenExtensionsConfigurator(
    private val extensions: TokenMetadataExtension,
    private val token: Token.Active
) : TokenConfigurator {

    override fun config(): TokenExtensions {
        var tokenExtensions = TokenExtensions()
        tokenExtensions = CanBeHiddenConfigurator(extensions, tokenExtensions).config()
        tokenExtensions = SetupTokenVisibilityConfigurator(extensions, tokenExtensions).config()
        tokenExtensions = CalculateInTotalBalanceConfigurator(extensions, tokenExtensions).config()
        tokenExtensions = SetupTokenCellVisibilityConfigurator(extensions, tokenExtensions).config()
        return tokenExtensions
    }
}
