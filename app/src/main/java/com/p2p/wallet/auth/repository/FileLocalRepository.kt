package com.p2p.wallet.auth.repository

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class FileLocalRepository(context: Context) : FileRepository {

    private val qrFolder: File

    init {
        val rootFolder = File(context.cacheDir.toString() + File.separator + "p2p")
        qrFolder = File(rootFolder, "qr")
    }

    override suspend fun saveQr(name: String, bitmap: Bitmap): File = withContext(Dispatchers.IO) {
        ensureQrFolderExists()

        val qrFile = getQrFile(name)
        FileOutputStream(qrFile).use { bitmap.compress(Bitmap.CompressFormat.PNG, 0, it) }
        return@withContext qrFile
    }

    private fun getQrFile(name: String): File = File(qrFolder, "$name.png")

    private fun ensureQrFolderExists() = qrFolder.mkdirs()
}