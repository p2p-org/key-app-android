package org.p2p.wallet.common.storage

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.contentValuesOf
import kotlinx.coroutines.withContext
import org.p2p.wallet.R
import org.p2p.core.dispatchers.CoroutineDispatchers
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Date

private const val PENDING = 1
private const val NOT_PENDING = 0

class FileRepository(
    private val context: Context,
    private val coroutineDispatchers: CoroutineDispatchers
) {

    private val pdfFolder: File
    private val miscFolder: File

    init {
        val rootFolder = context.getExternalFilesDir(null)
        pdfFolder = File(rootFolder, "pdf")
        miscFolder = File(rootFolder, "misc")
    }

    fun saveQr(name: String, bitmap: Bitmap, forSharing: Boolean): File? {
        val file = saveBitmapAsFile(bitmap, name, toPublic = !forSharing)
        bitmap.recycle()
        return file
    }

    fun savePdf(fileName: String, bytes: ByteArray): File {
        ensurePdfFolderExists()
        val pdfFolder = getPdfFile(fileName)
        BufferedOutputStream(FileOutputStream(pdfFolder)).use {
            it.write(bytes)
            it.flush()
        }
        return pdfFolder
    }

    suspend fun saveFileToMisc(fileNameWithExt: String, stream: InputStream) {
        withContext(coroutineDispatchers.io) {
            ensureMiscFolderExists()

            val newMiscFile = File(miscFolder, fileNameWithExt)
            newMiscFile.outputStream().use { fileOut ->
                stream.use { inStream ->
                    inStream.copyTo(fileOut)
                }
            }
        }
    }

    fun getFileFromMisc(fileNameWithExt: String): File? {
        return if (isFileExists(fileNameWithExt)) {
            File(miscFolder, fileNameWithExt)
        } else {
            null
        }
    }

    fun isFileExists(fileNameWithExt: String): Boolean {
        return File(pdfFolder, fileNameWithExt).exists() || File(miscFolder, fileNameWithExt).exists()
    }

    fun getPdfFile(fileName: String) = File(pdfFolder, "$fileName.pdf")

    private fun ensurePdfFolderExists() = pdfFolder.mkdirs()
    private fun ensureMiscFolderExists() = miscFolder.mkdirs()

    // PWN-3968
    @Suppress("DEPRECATION")
    fun saveBitmapAsFile(bitmap: Bitmap, name: String, toPublic: Boolean = false): File? {
        val fileName = "$name${Date().time}"
        val appName = context.getString(R.string.app_name)
        val mimeType = "image/png"
        try {
            val file: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val mainDir = File(
                    context.cacheDir,
                    appName
                )
                val resolver = context.contentResolver
                saveBitmapToFile(mainDir, fileName, bitmap).also { savedFile ->
                    if (toPublic) {
                        resolver.openInputStream(Uri.fromFile(savedFile))?.use { input ->
                            val contentValues = contentValuesOf(
                                MediaStore.Images.Media.DISPLAY_NAME to "$fileName.png",
                                MediaStore.Images.Media.MIME_TYPE to mimeType,
                                MediaStore.Images.Media.RELATIVE_PATH to "${Environment.DIRECTORY_PICTURES}/$appName",
                                MediaStore.Images.Media.IS_PENDING to PENDING,
                            )

                            val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                            val uri = resolver.insert(collection, contentValues)
                                ?: throw IOException("Unable to get Uri: $contentValues")
                            resolver.openOutputStream(uri)?.use { out ->
                                input.copyTo(out)
                            }

                            contentValues.clear()
                            contentValues.put(MediaStore.Images.Media.IS_PENDING, NOT_PENDING)
                            resolver.update(uri, contentValues, null, null)
                        }
                    }
                }
            } else {
                val mainDir = File(
                    if (toPublic) Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    else context.cacheDir,
                    appName
                )
                saveBitmapToFile(mainDir, fileName, bitmap)
            }

            if (toPublic) {
                // Add image to gallery
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.toString()),
                    arrayOf(mimeType),
                    null
                )
            }
            return file
        } catch (e: IOException) {
            Timber.e(e, "Error on saving bitmap to file")
        }
        return null
    }
}

fun saveBitmapToFile(mainDir: File, fileName: String, bitmap: Bitmap): File {
    if (!mainDir.exists()) {
        mainDir.mkdir()
    }
    val stringPath = "${mainDir.absolutePath}/$fileName.png"
    return File(stringPath).also {
        val outputStream = FileOutputStream(it)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
    }
}
