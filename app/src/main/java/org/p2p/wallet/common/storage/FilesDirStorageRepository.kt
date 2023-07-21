package org.p2p.wallet.common.storage

import android.content.Context
import com.google.gson.Gson
import timber.log.Timber
import java.io.IOException
import kotlinx.coroutines.withContext
import org.p2p.core.dispatchers.DefaultDispatchers

class FilesDirStorageRepository(
    private val context: Context,
    private val gson: Gson,
    private val dispatchers: DefaultDispatchers
) : ExternalStorageRepository {

    // TODO: use toJson with streamed JsonReader instead of raw string
    override suspend fun <T> saveAsJsonFile(jsonObject: T, fileName: String) {
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
    override suspend fun readJsonFile(filePrefix: String): ExternalFile? = withContext(dispatchers.io) {
        val filesDirectory = context.filesDir
        val file = filesDirectory.listFiles { file ->
            file.name.startsWith(filePrefix)
        }
            ?.firstOrNull()

        if (file == null || !file.exists()) {
            Timber.i(IOException("Error reading json file: $filePrefix - file does not exist"))
            return@withContext null
        }

        try {
            context.openFileInput(file.name)
                .bufferedReader()
                .useLines { lines ->
                    val joinedLines = lines.joinToString()
                    Timber.i("File $filePrefix is read successfully: ${joinedLines.length}")
                    ExternalFile(joinedLines)
                }
        } catch (e: Throwable) {
            Timber.e(e, "Error reading json file: $filePrefix")
            null
        }
    }
}
