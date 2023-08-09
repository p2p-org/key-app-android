package org.p2p.token.service.repository.configurator

import org.p2p.core.token.Token
import org.p2p.core.token.TokenExtensions
import org.p2p.core.token.TokenMetadataExtension

@Suppress("UNCHECKED_CAST")
class TokenExtensionsConfigurator<T : Token>(
    private val extensions: TokenMetadataExtension,
    private val token: T
) : TokenConfigurator<T> {

    override fun config(): T {
        var tokenExtensions =
            TokenExtensions(tokenPercentDifferenceOnWalletScreen = extensions.percentDifferenceToShowByPriceOnWs)
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
        return when (token) {
            is Token.Active -> {
                val newToken = PricePercentDifferenceToShow(extensions, token).config()
                newToken.copy(tokenExtensions = tokenExtensions)
            }
            is Token.Other -> {
                token.copy(tokenExtensions = tokenExtensions)
            }
            is Token.Eth -> token
            else -> token
        } as T
    }
}
