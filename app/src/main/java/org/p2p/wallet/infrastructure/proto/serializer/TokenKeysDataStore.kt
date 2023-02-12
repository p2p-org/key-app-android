package org.p2p.wallet.infrastructure.proto.serializer

import android.content.Context
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import org.p2p.wallet.TokenKeysDataStore
import java.io.InputStream
import java.io.OutputStream

private const val STORE_FILE_NAME = "token_keys_store.pb"

class TokenKeysDataStoreSerializer(
    private val context: Context
) {

    private val Context.dataStore by dataStore(
        fileName = STORE_FILE_NAME,
        serializer = TokenKeysDataStoreSerializer
    )

    private object TokenKeysDataStoreSerializer : Serializer<TokenKeysDataStore> {

        override val defaultValue: TokenKeysDataStore
            get() = TokenKeysDataStore.getDefaultInstance()

        override suspend fun readFrom(input: InputStream): TokenKeysDataStore {
            return TokenKeysDataStore.parseFrom(input)
        }

        override suspend fun writeTo(store: TokenKeysDataStore, output: OutputStream) {
            store.writeTo(output)
        }
    }
}
