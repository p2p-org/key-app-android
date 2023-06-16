package org.p2p.wallet.common.feature_toggles.toggles.inapp

sealed class InAppFeatureFlag {
    abstract val featureName: String
    abstract var featureValue: Boolean

    abstract class InAppFeatureFlagString : InAppFeatureFlag() {
        override var featureValue: Boolean = false
        abstract val featureValueString: String?
    }
}
