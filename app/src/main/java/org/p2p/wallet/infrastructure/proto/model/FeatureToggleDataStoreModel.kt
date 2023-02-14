package org.p2p.wallet.infrastructure.proto.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import org.p2p.wallet.FeatureToggleDataStore
import org.p2p.wallet.infrastructure.proto.DataStoreModelContract
import java.io.InputStream
import java.io.OutputStream

private const val STORE_FILE_NAME = "feature_toggle_data_store.pb"

class FeatureToggleDataStoreModel(
    private val context: Context
) : DataStoreModelContract<FeatureToggleDataStore> {

    override fun getDataStoreModel(): DataStore<FeatureToggleDataStore> {
        return context.dataStore
    }

    private val Context.dataStore by dataStore(
        fileName = STORE_FILE_NAME,
        serializer = FeatureToggleDataStoreSerializer
    )

    private object FeatureToggleDataStoreSerializer : Serializer<FeatureToggleDataStore> {

        override val defaultValue: FeatureToggleDataStore
            get() = FeatureToggleDataStore.getDefaultInstance()

        override suspend fun readFrom(input: InputStream): FeatureToggleDataStore {
            return FeatureToggleDataStore.parseFrom(input)
        }

        override suspend fun writeTo(store: FeatureToggleDataStore, output: OutputStream) {
            store.writeTo(output)
        }
    }
}
