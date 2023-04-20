package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class UsernameDomainFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : StringFeatureToggle(valuesProvider) {
    override val featureKey: String = "username_domain"
    override val featureDescription: String = "Username domain postfix"
    override val defaultValue: String = ".key.sol"
}
