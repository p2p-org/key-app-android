package com.p2p.wallet.auth.repository

import android.graphics.Bitmap
import java.io.File

interface FileRepository {

    suspend fun saveQr(name: String, bitmap: Bitmap): File
}