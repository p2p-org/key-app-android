package com.p2p.wowlet.datastore

import com.p2p.wowlet.entities.enums.SelectedCurrency
import com.p2p.wowlet.entities.local.EnableFingerPrintModel
import com.p2p.wowlet.entities.local.EnableNotificationModel
import com.p2p.wowlet.entities.local.PinCodeData
import com.p2p.wowlet.entities.local.UserSecretData
import com.p2p.wowlet.entities.local.WalletItem
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