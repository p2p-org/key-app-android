package org.p2p.wallet.auth.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
        val appName = context.getString(R.string.app_name)
        try {
            val file: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$appName")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val uri = resolver.insert(collection, contentValues)
                    ?: throw IOException("Unable to get Uri: $contentValues")
                resolver.openOutputStream(uri).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                resolver.openOutputStream(uri) ?: throw IOException("Unable to open OutputStream from uri: $uri")
                File(uri.toString())
            } else {
                val mainDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    appName
                )
                if (!mainDir.exists()) {
                    mainDir.mkdir()
                }
                val stringPath = "${mainDir.absolutePath}/$fileName.png"
                File(stringPath).also {
                    val outputStream = FileOutputStream(it)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                }
            }
            // Add image to gallery
            MediaScannerConnection.scanFile(context, arrayOf(file.toString()), null, null)
            return file
        } catch (e: IOException) {
            Timber.e(e, "Error on saving bitmap to file")
        }
        return null
    }

    private fun getUriFromPath(displayName: String): Uri? {
        val photoId: Long
        val photoUri = MediaStore.Images.Media.getContentUri("external")
        val projection = arrayOf(MediaStore.Images.ImageColumns._ID)
        val cursor = context.contentResolver.query(
            photoUri,
            projection,
            MediaStore.Images.ImageColumns.DISPLAY_NAME + " LIKE ?",
            arrayOf(displayName),
            null
        )!!
        cursor.moveToFirst()
        val columnIndex = cursor.getColumnIndex(projection[0])
        photoId = cursor.getLong(columnIndex)
        cursor.close()
        return Uri.parse("$photoUri/$photoId")
    }
}
