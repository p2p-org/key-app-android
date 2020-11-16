package com.wowlet.data.datastore

import com.wowlet.entities.local.PinCodeData
import com.wowlet.entities.local.UserSecretData


interface PreferenceService {
    fun setSecretDataInFile( userData: UserSecretData)
    fun getSecretDataAtFile(): UserSecretData?
    fun setPinCodeValue(codeValue: PinCodeData)
    fun getPinCodeValue(): PinCodeData?
    fun enableNotification(isEnable:Boolean)
    fun isAllowNotification():Boolean
    fun isFinishReg():Boolean
    fun finishReg(finishReg:Boolean)
}