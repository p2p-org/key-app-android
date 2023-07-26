package org.p2p.token.service.repository.configurator

import org.p2p.core.token.TokenMetadataExtension
import org.p2p.core.token.Token
import org.p2p.core.token.TokenExtensions

class TokenExtensionsConfigurator(
    private val extensions: TokenMetadataExtension,
    private val token: Token.Active
) : TokenConfigurator<Token.Active> {

    override fun config(): Token.Active {
        var tokenExtensions = TokenExtensions()
        /**
         * Setup [Token.canBeHidden] configuration
         */
        tokenExtensions = CanBeHiddenConfigurator(extensions, tokenExtensions).config()
        /**
         * Setup [Token.isVisibleOnWalletScreen] configuration
         */
        tokenExtensions = SetupTokenVisibilityConfigurator(extensions, tokenExtensions).config()
        /**
         * Setup [Token.calculateInTotalBalance] configuration
         */
        tokenExtensions = CalculateInTotalBalanceConfigurator(extensions, tokenExtensions).config()
        /**
         * Setup [Token.isTokenCellVisibleOnWalletScreen] configuration
         */
        tokenExtensions = SetupTokenCellVisibilityConfigurator(extensions, tokenExtensions).config()
        /**
         * Setup [Token.ruleProcessingToken] configuration
         */
        tokenExtensions = RuleProcessingTokenConfigurator(extensions, tokenExtensions).config()
        /**
         * Setup [Token.numbersAfterDecimalPoint] setup count of numbers after decimal point
         */
        tokenExtensions = RuleOfFractionalPartConfigurator(extensions, tokenExtensions).config()
        /**
         * Setup [Token.setupPercentDifferenceToShowByPrice] configuration
         */
        val newToken = PercentDifferenceToShowByPriceConfigurator(extensions, token).config()

        return newToken.copy(tokenExtensions = tokenExtensions)
    }
}
