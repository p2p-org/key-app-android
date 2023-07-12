package org.p2p.wallet.common.storage

import android.content.Context
import timber.log.Timber
import java.io.File

class ExternalStorageRepository(
    private val context: Context
) {

    fun saveJson(json: String, fileName: String) {
        try {
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(json.toByteArray())
            }
        } catch (e: Throwable) {
            Timber.e(e, "Error saving json file: $fileName")
        }
    }

    fun readJsonFile(filePrefix: String): ExternalFile? {
        val directory = context.filesDir
        val file = directory
            .listFiles { file ->
                file.name.startsWith(filePrefix)
            }
            ?.firstOrNull()

        if (file == null || !file.exists()) {
            Timber.e("Error reading json file: $filePrefix - file does not exist")
            return null
        }

        return try {
            context.openFileInput(file.name).bufferedReader().useLines { lines ->
                val lastModified = File(context.filesDir, filePrefix).lastModified()
                ExternalFile(lines.joinToString(""), filePrefix)
            }
        } catch (e: Throwable) {
            Timber.e(e, "Error reading json file: $filePrefix")
            null
        }
    }
}
