package org.p2p.wallet.common.storage

interface ExternalStorageRepository {
    suspend fun <T> saveAsJsonFile(jsonObject: T, fileName: String)
    suspend fun readJsonFile(filePrefix: String): ExternalFile?
}
