package com.wowlet.domain.interactors

import android.graphics.Bitmap
import com.wowlet.entities.local.SecretKeyCombinationSuccess
import com.wowlet.entities.local.SecretKeyItem

interface DashboardInteractor {
   fun generateQRrCode(): Bitmap
}