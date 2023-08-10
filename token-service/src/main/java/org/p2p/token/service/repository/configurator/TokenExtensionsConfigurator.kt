package org.p2p.token.service.repository.configurator

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.core.token.TokenExtensions
import org.p2p.core.token.TokenMetadataExtension

class TokenExtensionsConfigurator(
    private val extensions: TokenMetadataExtension,
    private val tokenTotal: BigDecimal? = null,
    private val tokenRate: BigDecimal? = null
) : TokenConfigurator {

    override fun config(): TokenExtensions {
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
        if (tokenTotal != null) {
            tokenExtensions = PricePercentDifferenceToShow(extensions, tokenExtensions, tokenTotal, tokenRate).config()
        }

        return tokenExtensions
    }
}
