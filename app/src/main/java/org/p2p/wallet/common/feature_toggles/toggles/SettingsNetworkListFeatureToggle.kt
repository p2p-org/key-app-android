package org.p2p.wallet.common.feature_toggles.toggles

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.p2p.wallet.common.feature_toggles.JsonFeatureToggle
import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesSource
import org.p2p.wallet.utils.fromJsonReified

class SettingsNetworkValue(
    @SerializedName("urlString")
    val url: String,
    @SerializedName("network")
    val networkName: String,
    @SerializedName("additionalQuery")
    val additionalQuery: String? = null
)

class SettingsNetworkListFeatureToggle(
    private val gson: Gson,
    valuesProvider: RemoteConfigValuesSource
) : JsonFeatureToggle<List<SettingsNetworkValue>>(valuesProvider) {
    override val toggleKey: String = "settings_network_values"
    override val toggleDescription: String = "List of available networks to choose from"

    override val defaultValue: List<SettingsNetworkValue> = emptyList()
    override val value: List<SettingsNetworkValue>
        get() = valuesProvider.getString(toggleKey)
            ?.let { gson.fromJsonReified(it) }
            ?: defaultValue
}
