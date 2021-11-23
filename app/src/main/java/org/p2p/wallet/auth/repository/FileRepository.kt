package org.p2p.wallet.auth.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

class FileRepository(
    private val context: Context
) {

    private val pdfFolder: File
    private val qrFolder: File

    init {
        val rootFolder = context.getExternalFilesDir(null)
        pdfFolder = File(rootFolder, "pdf")
        qrFolder = getQrFolder()
    }

    suspend fun saveQr(name: String, bitmap: Bitmap): File = withContext(Dispatchers.IO) {
        ensureQrFolderExists()
        val qrFile = getQrFile(name)
        FileOutputStream(qrFile).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return@withContext qrFile
    }

    fun savePdf(
        fileName: String,
        bytes: ByteArray
    ): File {
        ensurePdfFolderExists()
        val pdfFolder = getPdfFile(fileName)
        BufferedOutputStream(FileOutputStream(pdfFolder)).use {
            it.write(bytes)
            it.flush()
        }
        return pdfFolder
    }

    private fun getPdfFile(fileName: String) = File(pdfFolder, "$fileName.pdf")

    private fun getQrFile(name: String): File = File(qrFolder, "$name.png")

    private fun ensureQrFolderExists() = qrFolder.mkdirs()

    private fun ensurePdfFolderExists() = pdfFolder.mkdirs()

    private fun getQrFolder(): File =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "myQr")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).toString()
            File(uri)
        } else {
            @Suppress("DEPRECATION")
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val pathName = directory.toString() + File.separator + "p2p-wallet"
            File(pathName)
        }
}