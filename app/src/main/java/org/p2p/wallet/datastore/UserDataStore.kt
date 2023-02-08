package org.p2p.wallet.datastore

import kotlinx.coroutines.flow.Flow

interface UserDataStore {

    suspend fun edit(block: (Editor) -> Unit)

    fun getStringFlow(key: String): Flow<String?>
    fun getIntFlow(key: String): Flow<Int?>
    fun getBooleanFlow(key: String): Flow<Boolean>
    fun getDoubleFlow(key: String): Flow<Double?>

    interface Editor {
        suspend fun removeString(key: String)
        suspend fun removeInt(key: String)
        suspend fun removeDouble(key: String)
        suspend fun removeBoolean(key: String)
        suspend fun putString(key: String, value: String)
        suspend fun putBoolean(key: String, value: Boolean)
        suspend fun putInt(key: String, value: Int)
    }
}
