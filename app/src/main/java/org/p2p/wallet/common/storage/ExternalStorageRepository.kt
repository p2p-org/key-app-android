package org.p2p.wallet.common.storage

import java.io.InputStream

interface ExternalStorageRepository {
    fun isFileExists(fileName: String): Boolean
    suspend fun saveRawFile(fileName: String, body: InputStream)
    suspend fun saveRawFile(fileName: String, body: String)
    suspend fun <T> saveAsJsonFile(fileName: String, jsonObject: T)
    suspend fun readJsonFile(fileName: String): ExternalFile?
    fun deleteFile(fileName: String)
}
