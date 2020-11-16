package com.wowlet.domain.usecases

import android.graphics.Bitmap
import com.wowlet.data.datastore.DashboardRepository
import com.wowlet.data.datastore.PreferenceService
import com.wowlet.domain.interactors.DashboardInteractor
import com.wowlet.domain.interactors.PinCodeInteractor
import com.wowlet.domain.interactors.SecretKeyInteractor
import com.wowlet.entities.local.PinCodeData
import com.wowlet.entities.local.SecretKeyCombinationSuccess
import com.wowlet.entities.local.SecretKeyItem


class DashBoardUseCase(private val dashboardRepository: DashboardRepository, private val preferenceService: PreferenceService) : DashboardInteractor {

    override fun generateQRrCode(): Bitmap {
        val publickKey=preferenceService.getSecretDataAtFile()?.publicKey?:""
        return dashboardRepository.getQrCode(publickKey)
    }
}