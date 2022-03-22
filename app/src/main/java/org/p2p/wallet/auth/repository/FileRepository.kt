package org.p2p.wallet.auth.repository

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import org.p2p.wallet.R
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date

class FileRepository(private val context: Context) {

    private val pdfFolder: File

    init {
        val rootFolder = context.getExternalFilesDir(null)
        pdfFolder = File(rootFolder, "pdf")
    }

    fun saveQr(name: String, bitmap: Bitmap): File? {
        val file = saveBitmapAsFile(bitmap, name)
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

    private fun getPdfFile(fileName: String) = File(pdfFolder, "$fileName.pdf")

    private fun ensurePdfFolderExists() = pdfFolder.mkdirs()

    fun saveBitmapAsFile(bitmap: Bitmap, name: String? = null): File? {
        val fileName = name ?: Date().toString()
        try {
            val mainDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                context.getString(R.string.app_name)
            )
            if (!mainDir.exists()) {
                mainDir.mkdir()
            }
            val stringPath = mainDir.absolutePath + "/$fileName" + ".png"
            val imageFile = File(stringPath)
            val fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
            return imageFile
        } catch (e: IOException) {
            Timber.e(e, "Error on saving bitmap to file")
        }
        return null
    }
}
