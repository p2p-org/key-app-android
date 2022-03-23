package org.p2p.wallet.infrastructure.security

import org.p2p.wallet.common.crypto.keystore.DecodeCipher
import org.p2p.wallet.common.crypto.keystore.EncodeCipher

interface SecureStorageContract {
    fun saveString(key: String, data: String)
    fun saveString(key: String, data: String, cipher: EncodeCipher)
    fun getString(key: String): String?
    fun getString(key: String, cipher: DecodeCipher): String?
    fun saveBytes(key: String, data: ByteArray)
    fun getBytes(key: String): ByteArray?
    fun remove(key: String)
    fun contains(key: String): Boolean
    fun clear()
}
