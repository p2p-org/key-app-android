package org.p2p.wallet.infrastructure.account

import kotlin.reflect.KClass

interface AccountStorageContract {
    fun <T> saveObject(key: String, data: T)
    fun <T : Any> getObject(key: String, type: KClass<T>): T?
    fun contains(key: String): Boolean
}
