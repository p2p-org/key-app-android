package org.p2p.wallet.debug.featuretoggles

data class FeatureToggleRowItem(
    val toggleName: String,
    val toggleValue: String,
    val isBooleanToggle: Boolean,
    val isInAppFlag: Boolean,
    val canBeChanged: Boolean
)
