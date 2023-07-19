package org.p2p.core.token

import android.os.Parcelable
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
    val tokenPercentDifferenceOnWalletScreen: Int? = null,
    val isCalculateWithTotalBalance: Boolean? = null,
    val tokenFractionRuleOnWalletScreen: String? = null,
    val canTokenBeHidden: Boolean? = null
) : Parcelable
