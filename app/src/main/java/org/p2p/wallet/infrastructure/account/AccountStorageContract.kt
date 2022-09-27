package org.p2p.wallet.infrastructure.account

import kotlin.reflect.KClass

interface AccountStorageContract {
    fun <T> saveObject(key: String, data: T)
    fun <T : Any> getObject(key: String, type: KClass<T>): T?
    fun saveString(key: String, data: String)
    fun getString(key: String): String?
    fun contains(key: String): Boolean
    fun remove(key: String)
    fun removeAll()
}
