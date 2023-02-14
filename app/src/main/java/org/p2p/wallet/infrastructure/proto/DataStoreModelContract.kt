package org.p2p.wallet.infrastructure.proto

import androidx.datastore.core.DataStore

interface DataStoreModelContract<T> {
    fun getDataStoreModel(): DataStore<T>
}
