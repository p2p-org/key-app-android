package org.p2p.wallet.infrastructure.proto.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import org.p2p.wallet.OnboardMetaDataStore
import org.p2p.wallet.OnboardMetadataDataStore
import org.p2p.wallet.infrastructure.proto.DataStoreModelContract
import java.io.InputStream
import java.io.OutputStream

private const val STORE_FILE_NAME = "onboard_meta_data_store.pb"

class OnboardMetaDataStoreModel(
    private val context: Context
): DataStoreModelContract<OnboardMetaDataStore> {

    private val Context.dataStore by dataStore(
        fileName = STORE_FILE_NAME,
        serializer = OnboardMetaDataStoreSerializer
    )

    override fun getDataStoreModel(): DataStore<OnboardMetaDataStore> {
        return context.dataStore
    }

    private object OnboardMetaDataStoreSerializer: Serializer<OnboardMetaDataStore> {

        override val defaultValue: OnboardMetaDataStore
            get() = OnboardMetaDataStore.getDefaultInstance()

        override suspend fun readFrom(input: InputStream): OnboardMetaDataStore {
            return OnboardMetaDataStore.parseFrom(input)
        }

        override suspend fun writeTo(store: OnboardMetaDataStore, output: OutputStream) {
            store.writeTo(output)
        }
    }
}
