package org.p2p.wallet.common.crypto.keystore

import org.p2p.solanaj.utils.crypto.Hex
import java.security.KeyStore
import javax.crypto.Cipher

private const val ANDROID_KEY_STORE = "AndroidKeyStore"

class KeyStoreWrapper(private val encoderDecoder: EncoderDecoder) {

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }

    init {
        encoderDecoder.setKeyStore(keyStore, ANDROID_KEY_STORE)
    }

    fun encode(keyAlias: String, data: String): String {
        val bytes = encoderDecoder.encode(keyAlias, data.toByteArray())
        return Hex.encode(bytes)
    }

    fun encode(cipher: EncodeCipher, data: String): String {
        val bytes = cipher.value.doFinal(data.toByteArray())
        return Hex.encode(bytes)
    }

    fun decode(keyAlias: String, encoded: String): String {
        val bytes = encoderDecoder.decode(keyAlias, Hex.decode(encoded))
        return String(bytes)
    }

    fun decode(cipher: DecodeCipher, encoded: String): String {
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

    fun delete(keyAlias: String) {
        keyStore.deleteEntry(keyAlias)
        encoderDecoder.onKeyDeleted(keyAlias)
    }

    fun clear() {
        for (alias in keyStore.aliases()) {
            delete(alias)
        }
    }
}
