package org.p2p.wallet.infrastructure.proto.serializer

import android.content.Context
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import org.p2p.wallet.AuthDataStore
import java.io.InputStream
import java.io.OutputStream

private const val STORE_FILE_NAME = "auth_store.pb"

class AuthDataStore(
    private val context: Context
) {

    private val Context.dataStore by dataStore(
        fileName = STORE_FILE_NAME,
        serializer = AuthDataStoreSerializer
    )

    private object AuthDataStoreSerializer : Serializer<AuthDataStore> {

        override val defaultValue: AuthDataStore
            get() = AuthDataStore.getDefaultInstance()

        override suspend fun readFrom(input: InputStream): AuthDataStore {
            return AuthDataStore.parseFrom(input)
        }

        override suspend fun writeTo(store: AuthDataStore, output: OutputStream) {
            store.writeTo(output)
        }
    }
}
