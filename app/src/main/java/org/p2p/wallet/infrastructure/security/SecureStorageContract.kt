package org.p2p.wallet.infrastructure.security

import org.p2p.wallet.common.crypto.keystore.DecodeCipher
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import kotlin.reflect.KClass

interface SecureStorageContract {
    fun saveString(key: String, data: String)
    fun saveString(key: String, data: String, cipher: EncodeCipher)
    fun <T> saveObject(key: String, data: T)
    fun <T> saveObjectList(key: String, data: List<T>)
    fun getString(key: String): String?
    fun getString(key: String, cipher: DecodeCipher): String?
    fun <T : Any> getObject(key: String, type: KClass<T>): T?
    fun <T : Any> getObjectList(key: String): List<T>
    fun saveBytes(key: String, data: ByteArray)
    fun getBytes(key: String): ByteArray?
    fun remove(key: String)
    fun contains(key: String): Boolean
    fun clear()
}
