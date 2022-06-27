package org.p2p.wallet.debug.featuretoggles

data class FeatureToggleRow(
    val toggleName: String,
    val toggleValue: String,
    val isCheckable: Boolean,
    val canBeChanged: Boolean
)
