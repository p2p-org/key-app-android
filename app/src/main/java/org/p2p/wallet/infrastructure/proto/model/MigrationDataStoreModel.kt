package org.p2p.wallet.infrastructure.proto.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import org.p2p.wallet.PreferencesMigrationDataStore
import org.p2p.wallet.infrastructure.proto.DataStoreModelContract
import java.io.InputStream
import java.io.OutputStream

private const val STORE_FILE_NAME = "migration_data_store.pb"

class MigrationDataStoreModel(
    private val context: Context
) : DataStoreModelContract<PreferencesMigrationDataStore> {

    private val Context.dataStore by dataStore(
        fileName = STORE_FILE_NAME,
        serializer = MigrationDataStoreSerializer
    )

    override fun getDataStoreModel(): DataStore<PreferencesMigrationDataStore> {
        return context.dataStore
    }

    private object MigrationDataStoreSerializer : Serializer<PreferencesMigrationDataStore> {

        override val defaultValue: PreferencesMigrationDataStore
            get() = PreferencesMigrationDataStore.getDefaultInstance()

        override suspend fun readFrom(input: InputStream): PreferencesMigrationDataStore {
            return PreferencesMigrationDataStore.parseFrom(input)
        }

        override suspend fun writeTo(store: PreferencesMigrationDataStore, output: OutputStream) {
            store.writeTo(output)
        }
    }
}
