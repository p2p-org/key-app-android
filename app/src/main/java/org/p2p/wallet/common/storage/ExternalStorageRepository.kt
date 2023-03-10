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

    fun readJsonFile(fileName: String): ExternalFile? {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) {
            Timber.e("Error reading json file: $fileName - file does not exist")
            return null
        }

        return try {
            context.openFileInput(fileName).bufferedReader().useLines { lines ->
                val lastModified = File(context.filesDir, fileName).lastModified()
                ExternalFile(lines.joinToString(""), lastModified)
            }
        } catch (e: Throwable) {
            Timber.e(e, "Error reading json file: $fileName")
            null
        }
    }
}
