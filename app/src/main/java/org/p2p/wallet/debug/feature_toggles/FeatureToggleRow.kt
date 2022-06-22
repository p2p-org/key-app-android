package org.p2p.wallet.debug.feature_toggles

data class FeatureToggleRow(
    val toggleName: String,
    val toggleDescription: String,
    val toggleValue: String,
    val isCheckable: Boolean,
    val canBeChanged: Boolean
)
