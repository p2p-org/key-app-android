package org.p2p.wallet.common.feature_toggles.remote_config

interface RemoteConfigValuesProvider {
    fun getString(toggleKey: String): String?
    fun getBoolean(toggleKey: String): Boolean?
    fun getFloat(toggleKey: String): Float?
    fun getInt(toggleKey: String): Int?
    fun getLong(toggleKey: String): Long?
}
