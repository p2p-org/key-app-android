package org.p2p.wallet.common.feature_toggles.toggles.inapp

sealed class InAppFeatureFlag {
    abstract val featureName: String
    abstract var featureValue: Boolean

    // todo: find another good solution for this, not a good implementation
    //  it's not done properly due to lack of time
    abstract class InAppFeatureFlagString : InAppFeatureFlag() {
        override var featureValue: Boolean = false
        abstract val featureValueString: String?
    }
}
