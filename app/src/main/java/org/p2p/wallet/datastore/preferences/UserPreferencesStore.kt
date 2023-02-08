package org.p2p.wallet.datastore.preferences

interface UserPreferencesStore {

    suspend fun getString(key: String): String?
    suspend fun getInt(key: String): Int?
    suspend fun getBoolean(key: String): Boolean
    suspend fun getDouble(key: String): Double?


    suspend fun putString(key: String, value: String)
    suspend fun putInt(key: String, value: Int)
    suspend fun putBoolean(key: String, value: Boolean)
    suspend fun putDouble(key: String, value: Double)

    suspend fun contains(key: String): Boolean
    suspend fun remove(key: String)
    suspend fun clear()
}
