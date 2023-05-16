package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class SocketSubscriptionsFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : BooleanFeatureToggle(valuesProvider) {
    override val featureKey: String = "android_socket_subscriptions_enabled"
    override val featureDescription: String = "Enable updating tokens and balance by sockets"
    override val defaultValue: Boolean = true
}
