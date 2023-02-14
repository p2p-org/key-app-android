package org.p2p.wallet.infrastructure.proto.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import org.p2p.wallet.WalletKeysDataStore
import org.p2p.wallet.infrastructure.proto.DataStoreModelContract
import java.io.InputStream
import java.io.OutputStream

private const val STORE_FILE_NAME = "wallet_keys_store.pb"

class WalletKeysDataStoreModel(
    private val context: Context
) : DataStoreModelContract<WalletKeysDataStore> {

    private val Context.dataStore by dataStore(
        fileName = STORE_FILE_NAME,
        serializer = WalletKeysDataStoreSerializer
    )

    override fun getDataStoreModel(): DataStore<WalletKeysDataStore> {
        return context.dataStore
    }

    private object WalletKeysDataStoreSerializer : Serializer<WalletKeysDataStore> {

        override val defaultValue: WalletKeysDataStore
            get() = WalletKeysDataStore.getDefaultInstance()

        override suspend fun readFrom(input: InputStream): WalletKeysDataStore {
            return WalletKeysDataStore.parseFrom(input)
        }

        override suspend fun writeTo(store: WalletKeysDataStore, output: OutputStream) {
            store.writeTo(output)
        }
    }
}
