package org.p2p.wallet.auth.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.format.DateFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.wallet.R
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.Date

class FileRepository(
    private val context: Context
) {

    private val pdfFolder: File

    init {
        val rootFolder = context.getExternalFilesDir(null)
        pdfFolder = File(rootFolder, "pdf")
    }

    @Throws(IOException::class)
    suspend fun saveQr(name: String, bitmap: Bitmap) = withContext(Dispatchers.IO) {
        val stream: OutputStream = generateOutputStream(name)
            ?: throw IllegalStateException("Couldn't save qr image")

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()
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

    private fun ensurePdfFolderExists() = pdfFolder.mkdirs()

    private fun generateOutputStream(name: String): OutputStream? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            val directory = Environment.DIRECTORY_DCIM + File.separator + "p2p-wallet"
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, directory)
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
            resolver.openOutputStream(uri)
        } else {
            @Suppress("DEPRECATION")
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            val pathName = directory.toString() + File.separator + "p2p-wallet"
            FileOutputStream(File(pathName))
        }

    fun takeScreenShot(bitmap: Bitmap): File? {
        val date = Date()
        val format = DateFormat.format("MM-dd-yyyy_hh:mm:ss", date)
        try {
            val mainDir =
                File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), context.getString(R.string.app_name))
            if (!mainDir.exists()) {
                val mkdir = mainDir.mkdir()
            }

            val stringPath = mainDir.absolutePath + "/$date" + ".jpeg"
            val imageFile = File(stringPath)
            val fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
            return imageFile
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}