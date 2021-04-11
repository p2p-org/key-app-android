package com.p2p.wowlet.infrastructure.persistence

import com.p2p.wowlet.dashboard.model.SelectedCurrency
import com.p2p.wowlet.dashboard.model.local.EnableFingerPrintModel
import com.p2p.wowlet.dashboard.model.local.EnableNotificationModel
import com.p2p.wowlet.dashboard.model.local.PinCodeData
import com.p2p.wowlet.dashboard.model.local.UserSecretData
import com.p2p.wowlet.dashboard.model.local.WalletItem
import org.p2p.solanaj.rpc.Cluster

@Deprecated("Workaround, remove unnecessary data storing logic, remove interfaces")
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