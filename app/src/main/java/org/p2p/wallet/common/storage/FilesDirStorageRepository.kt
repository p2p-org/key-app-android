package org.p2p.wallet.common.storage

import android.content.Context
import com.google.gson.Gson
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.withContext
import org.p2p.core.dispatchers.DefaultDispatchers

class FilesDirStorageRepository(
    private val context: Context,
    private val gson: Gson,
    private val dispatchers: DefaultDispatchers
) : ExternalStorageRepository {

    override fun isFileExists(fileName: String): Boolean {
        val file = findFile(fileName)
        return file != null && file.exists() && file.canRead()
    }

    override suspend fun saveRawFile(fileName: String, body: InputStream) = withContext(dispatchers.io) {
        try {
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use(body::copyTo)
            Timber.i("File $fileName saved")
        } catch (e: Throwable) {
            Timber.e(e, "Error saving file: $fileName")
        }
    }

    override suspend fun saveRawFile(fileName: String, body: String) {
        withContext(dispatchers.io) {
            try {
                context.openFileOutput(fileName, Context.MODE_PRIVATE)
                    .bufferedWriter()
                    .use { it.write(body) }
                Timber.i("File $fileName saved")
            } catch (e: Throwable) {
                Timber.e(e, "Error saving file: $fileName")
            }
        }
    }

    // TODO: use toJson with streamed JsonReader instead of raw string
    override suspend fun <T> saveAsJsonFile(fileName: String, jsonObject: T) {
        withContext(dispatchers.io) {
            try {
                context.openFileOutput(fileName, Context.MODE_PRIVATE)
                    .bufferedWriter()
                    .use { gson.toJson(jsonObject, it) }
                Timber.i("File $fileName saved")
            } catch (e: Throwable) {
                Timber.e(e, "Error saving json file: $fileName")
            }
        }
    }

    // TODO: return optional InputStream instead of big raw string
    override suspend fun readJsonFile(fileName: String): ExternalFile? = withContext(dispatchers.io) {
        val file = findFile(fileName)

        if (file == null || !file.exists()) {
            Timber.i(IOException("Error reading json file: $fileName - file does not exist"))
            return@withContext null
        }

        try {
            context.openFileInput(file.name)
                .bufferedReader()
                .useLines { lines ->
                    val joinedLines = lines.joinToString("")
                    Timber.i("File $fileName is read successfully: ${joinedLines.length}")
                    ExternalFile(joinedLines)
                }
        } catch (e: Throwable) {
            Timber.e(e, "Error reading json file: $fileName")
            null
        }
    }

    override fun deleteFile(fileName: String) {
        val file = findFile(fileName)
        if (file == null || !file.exists()) {
            return
        }

        try {
            file.delete()
            Timber.i("File $fileName deleted")
        } catch (e: Throwable) {
            Timber.e(e, "Error deleting file: $fileName")
        }
    }

    private fun findFile(fileName: String): File? {
        val filesDirectory = context.filesDir
        return filesDirectory
            .listFiles { file ->
                file.name.startsWith(fileName)
            }
            ?.firstOrNull()
    }
}
