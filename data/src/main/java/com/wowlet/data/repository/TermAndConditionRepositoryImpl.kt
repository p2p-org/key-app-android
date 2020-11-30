package com.wowlet.data.repository

import android.graphics.Bitmap
import com.wowlet.data.datastore.DashboardRepository
import com.wowlet.data.datastore.TermAndConditionRepository
import com.wowlet.data.util.WalletDataConst

import com.wowlet.entities.Result
import com.wowlet.entities.local.*
import net.glxn.qrgen.android.QRCode


class TermAndConditionRepositoryImpl() : TermAndConditionRepository {
    override suspend fun initNewUser(): List<UserSecretData>  {
        TODO("Not yet implemented")
    }
}