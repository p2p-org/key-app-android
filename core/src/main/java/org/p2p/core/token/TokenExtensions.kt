package org.p2p.core.token

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

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
    val tokenPercentDifferenceOnWalletScreen: Double? = null,
    val isCalculateWithTotalBalance: Boolean? = null,
    val numbersAfterDecimalPoint: Int? = null,
    val canTokenBeHidden: Boolean? = null
) : Parcelable {

    // TODO: Make better solution for this
    @IgnoredOnParcel
    var isRateExceedsTheDifference: Boolean = true

    companion object {
        val NONE = TokenExtensions()
    }
}

fun List<Token.Active>.filterTokensForWalletScreen(): List<Token.Active> {
    return filter { it.tokenExtensions.isTokenVisibleOnWalletScreen == true }
}

fun List<Token.Active>.filterTokensForCalculationOfFinalBalance(): List<Token.Active> {
    return filter { it.tokenExtensions.isCalculateWithTotalBalance == true }
}

fun List<Token.Active>.filterTokensByAvailability(): List<Token.Active> {
    return filter { it.tokenExtensions.isTokenCellVisibleOnWalletScreen != false }
}
