package org.p2p.wallet.common.storage

import java.io.InputStream

interface ExternalStorageRepository {
    suspend fun <T> saveAsJsonFile(jsonObject: T, fileName: String)
    suspend fun readJsonFile(filePrefix: String): ExternalFile?
    suspend fun readJsonFileAsStream(filePrefix: String): InputStream?
    suspend fun saveAsJsonFile(stream: InputStream, fileName: String): Long
}
