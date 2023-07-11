package org.p2p.wallet.common.crypto.keystore

import android.content.SharedPreferences
import java.security.KeyStore
import javax.crypto.Cipher
import org.p2p.core.crypto.Hex
import org.p2p.core.wrapper.HexString

class KeyStoreWrapper(
    private val encoderDecoder: EncoderDecoder,
    private val keyStore: KeyStore,
    sharedPreferences: SharedPreferences
) {

    init {
        keyStore.apply { load(null) }
        encoderDecoder.init(
            keyStore = keyStore,
            providerName = keyStore.type,
            // to save iv vector
            preferences = sharedPreferences
        )
    }

    fun encode(keyAlias: String, data: String): HexString {
        val bytes = encoderDecoder.encode(keyAlias, data.toByteArray())
        return HexString(Hex.encode(bytes))
    }

    fun encode(cipher: EncodeCipher, data: String): HexString {
        val bytes = cipher.value.doFinal(data.toByteArray())
        return HexString(Hex.encode(bytes))
    }

    @Throws(IllegalArgumentException::class)
    fun decode(keyAlias: String, encoded: HexString): String {
        val bytes = encoderDecoder.decode(keyAlias, Hex.decode(encoded))
        return String(bytes)
    }

    fun decode(cipher: DecodeCipher, encoded: HexString): String {
        val bytes = cipher.value.doFinal(Hex.decode(encoded))
        return String(bytes)
    }

    fun getEncodeCipher(keyAlias: String): EncodeCipher {
        if (!contains(keyAlias)) encoderDecoder.createSecuredKey(keyAlias)
        return EncodeCipher(encoderDecoder.getCipher(keyAlias, Cipher.ENCRYPT_MODE))
    }

    fun getDecodeCipher(keyAlias: String): DecodeCipher {
        if (!contains(keyAlias)) encoderDecoder.createSecuredKey(keyAlias)
        return DecodeCipher(encoderDecoder.getCipher(keyAlias, Cipher.DECRYPT_MODE))
    }

    fun contains(keyAlias: String): Boolean = keyStore.containsAlias(keyAlias)

    fun deleteKeyAlias(keyAlias: String) {
        keyStore.deleteEntry(keyAlias)
        encoderDecoder.onKeyDeleted(keyAlias)
    }

    fun clear() {
        for (alias in keyStore.aliases()) {
            deleteKeyAlias(alias)
        }
    }
}
