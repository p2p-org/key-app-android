package org.p2p.token.service.repository.configurator

import org.p2p.core.token.MetadataExtension
import org.p2p.core.token.Token
import org.p2p.core.token.TokenExtensions

class TokenExtensionsConfigurator(private val extensions: MetadataExtension) : TokenConfigurator {

    override fun config(token: Token.Active): TokenExtensions {
        var tokenExtensions = TokenExtensions()
        tokenExtensions = CanBeHiddenConfigurator(extensions, tokenExtensions).config(token)
        tokenExtensions = SetupTokenVisibilityConfigurator(extensions, tokenExtensions).config(token)
        tokenExtensions = CaclulateInTotalBalanceConfigurator(extensions, tokenExtensions).config(token)
        tokenExtensions = SetupTokenCellVisibilityConfigurator(extensions, tokenExtensions).config(token)
        return tokenExtensions
    }
}
