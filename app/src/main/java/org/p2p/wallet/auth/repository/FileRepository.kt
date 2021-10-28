package org.p2p.wallet.auth.repository

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

class FileRepository(context: Context) {

    private val pdfFolder: File
    private val qrFolder: File

    init {
        val rootFolder = context.getExternalFilesDir(null)
        pdfFolder = File(rootFolder, "pdf")
        qrFolder = File(rootFolder, "qr")
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
}