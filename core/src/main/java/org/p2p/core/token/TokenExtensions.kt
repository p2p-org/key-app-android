package org.p2p.core.token

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.core.utils.Constants

/**
 * @param canTokenBeHidden [org.p2p.token.service.repository.configurator.CanBeHiddenConfigurator]
 * @param isTokenVisibleOnWalletScreen [org.p2p.token.service.repository.configurator.SetupTokenVisibilityConfigurator]
 * @param isTokenCellVisibleOnWalletScreen [org.p2p.token.service.repository.configurator.SetupTokenCellVisibilityConfigurator]
 */
@Parcelize
data class TokenExtensions(
    val ruleOfProcessingTokenPrice: String? = null,
    val isTokenVisibleOnWalletScreen: Boolean? = null,
    val isTokenCellVisibleOnWalletScreen: Boolean? = null,
    val tokenPercentDifferenceOnWalletScreen: Int? = null,
    val isCalculateWithTotalBalance: Boolean? = null,
    val numbersAfterDecimalPoint: Int? = null,
    val canTokenBeHidden: Boolean? = null
) : Parcelable {

    companion object {
        val NONE = TokenExtensions()
    }
}

fun List<Token.Active>.filterTokensForWalletScreen(): List<Token.Active> {
    return filter { it.isUSDC && it.mintAddress == Constants.USDC_MINT }
}
