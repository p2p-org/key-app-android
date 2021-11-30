package org.p2p.wallet.infrastructure.network.provider

import org.p2p.wallet.infrastructure.security.SecureStorageContract
import kotlinx.coroutines.runBlocking
import org.p2p.solanaj.utils.crypto.Base58Utils

private const val KEY_PUBLIC_KEY = "KEY_PUBLIC_KEY"
private const val KEY_SECRET_KEY = "KEY_SECRET_KEY"

class TokenKeyProvider(
    private val secureStorage: SecureStorageContract
) {

    var publicKey: String =
        runBlocking {
            val result = secureStorage.getString(KEY_PUBLIC_KEY).orEmpty()
            Base58Utils.decodeToString(result)
        }
        set(value) {
                field = value
                runBlocking {
                    val result = Base58Utils.encodeFromString(value)
                    secureStorage.saveString(KEY_PUBLIC_KEY, result)
                }
            }

    var secretKey: ByteArray =
        runBlocking {
            val result = secureStorage.getString(KEY_SECRET_KEY).orEmpty()
            Base58Utils.decode(result)
        }
        set(value) {
                field = value
                runBlocking {
                    val result = Base58Utils.encode(value)
                    secureStorage.saveString(KEY_SECRET_KEY, result)
                }
            }
}