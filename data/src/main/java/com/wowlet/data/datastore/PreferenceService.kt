package com.wowlet.data.datastore

import com.wowlet.entities.enums.SelectedCurrency
import com.wowlet.entities.local.*
import org.p2p.solanaj.rpc.Cluster

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
    fun setSelectedNetWork(cluster: Cluster)
    fun getSelectedCluster(): Cluster
    fun setSelectedCurrency(currency: SelectedCurrency)
    fun getSelectedCurrency(): SelectedCurrency?
}