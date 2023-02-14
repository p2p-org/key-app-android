package org.p2p.wallet.infrastructure.proto.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import org.p2p.wallet.AuthDataStore
import org.p2p.wallet.infrastructure.proto.DataStoreModelContract
import java.io.InputStream
import java.io.OutputStream

private const val STORE_FILE_NAME = "auth_store.pb"

class AuthDataStoreModel(
    private val context: Context
) : DataStoreModelContract<AuthDataStore> {

    private val Context.dataStore by dataStore(
        fileName = STORE_FILE_NAME,
        serializer = AuthDataStoreSerializer
    )

    override fun getDataStoreModel(): DataStore<AuthDataStore> {
        return context.dataStore
    }

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
