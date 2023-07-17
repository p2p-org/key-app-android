package org.p2p.wallet.home.ui.crypto

import java.math.BigDecimal
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.user.interactor.UserInteractor

class MyCryptoInteractor(
    private val userInteractor: UserInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val ethereumInteractor: EthereumInteractor
) {

    suspend fun setTokenHidden(mintAddress: String, visibility: String) {
        userInteractor.setTokenHidden(mintAddress, visibility)
    }

    fun getHiddenTokensVisibility(): Boolean {
        return userInteractor.getHiddenTokensVisibility()
    }

    fun setHiddenTokensVisibility(isVisible: Boolean) {
        userInteractor.setHiddenTokensVisibility(isVisible)
    }

    fun areZerosHidden(): Boolean {
        return settingsInteractor.areZerosHidden()
    }

    fun getClaimBundleById(latestActiveBundleId: String): BridgeBundle? {
        return ethereumInteractor.getClaimBundleById(latestActiveBundleId)
    }

    suspend fun getClaimMinAmountForFreeFee(): BigDecimal {
        return ethereumInteractor.getClaimMinAmountForFreeFee()
    }
}
