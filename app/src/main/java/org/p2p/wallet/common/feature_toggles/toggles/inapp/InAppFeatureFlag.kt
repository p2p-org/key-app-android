package org.p2p.wallet.common.feature_toggles.toggles.inapp

sealed class InAppFeatureFlag {
    abstract val featureName: String
    abstract var featureValue: Boolean
}
