package com.wowlet.data.datastore

import com.wowlet.entities.local.*

interface PreferenceService {
    fun setWalletItem(userData: UserSecretData)
    fun setSingleWalletData(userData: UserSecretData)
    fun getWalletList(): MutableList<UserSecretData>?
    fun getSingleWalletData(): UserSecretData?
    fun getActiveWallet(): UserSecretData?
    fun updateWallet(userSecretData: UserSecretData): Boolean
    fun setPinCodeValue(codeValue: PinCodeData): Boolean
    fun getPinCodeValue(): PinCodeData?
    fun enableNotification(isEnable: EnableNotificationModel)
    fun isAllowNotification(): EnableNotificationModel?
    fun isSetFingerPrint(): EnableFingerPrintModel?
    fun enableFingerPrint(isEnable: EnableFingerPrintModel)
    fun isCurrentLoginReg(): Boolean
    fun finishLoginReg(finishReg: Boolean)
    fun setWalletItemData(walletItem: WalletItem?)
    fun getWalletItemData(): WalletItem?
}