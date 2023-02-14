package org.p2p.wallet.infrastructure.proto.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import org.p2p.wallet.TorusDataStore
import org.p2p.wallet.infrastructure.proto.DataStoreModelContract
import java.io.InputStream
import java.io.OutputStream

private const val STORE_FILE_NAME = "torus_store.pb"

class TorusDataStoreModel(
    private val context: Context
) : DataStoreModelContract<TorusDataStore> {

    private val Context.dataStore by dataStore(
        fileName = STORE_FILE_NAME,
        serializer = TorusDataStoreSerializer
    )

    override fun getDataStoreModel(): DataStore<TorusDataStore> {
        return context.dataStore
    }

    private object TorusDataStoreSerializer : Serializer<TorusDataStore> {

        override val defaultValue: TorusDataStore
            get() = TorusDataStore.getDefaultInstance()

        override suspend fun readFrom(input: InputStream): TorusDataStore {
            return TorusDataStore.parseFrom(input)
        }

        override suspend fun writeTo(store: TorusDataStore, output: OutputStream) {
            store.writeTo(output)
        }
    }
}
