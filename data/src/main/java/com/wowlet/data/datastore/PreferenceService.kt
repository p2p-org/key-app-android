package com.wowlet.data.datastore

import com.wowlet.entities.local.EnableFingerPrintModel
import com.wowlet.entities.local.EnableNotificationModel
import com.wowlet.entities.local.PinCodeData
import com.wowlet.entities.local.UserSecretData

interface PreferenceService {
    fun setWalletItem(userData: UserSecretData)
    fun setSingleWalletData(userData: UserSecretData)
    fun getWalletList(): MutableList<UserSecretData>?
    fun getSingleWalletData(): UserSecretData?
    fun getActiveWallet(): UserSecretData?
    fun updateWallet(userSecretData: UserSecretData):Boolean
    fun setPinCodeValue(codeValue: PinCodeData):Boolean
    fun getPinCodeValue(): PinCodeData?
    fun enableNotification(isEnable: EnableNotificationModel)
    fun isAllowNotification(): EnableNotificationModel?
    fun isSetFingerPrint(): EnableFingerPrintModel?
    fun enableFingerPrint(isEnable: EnableFingerPrintModel)
    fun isCurrentLoginReg(): Boolean
    fun finishLoginReg(finishReg: Boolean)
}