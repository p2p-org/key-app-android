package org.p2p.wallet.home.ui.main

import java.math.BigDecimal
import kotlinx.coroutines.flow.StateFlow
import org.p2p.core.token.Token
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.striga.signup.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.user.interactor.UserInteractor

class HomeInteractor(
    private val userInteractor: UserInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val usernameInteractor: UsernameInteractor,
    private val sellInteractor: SellInteractor,
    private val metadataInteractor: MetadataInteractor,
    private val ethereumInteractor: EthereumInteractor,
    private val strigaUserInteractor: StrigaUserInteractor,
    private val strigaSignupInteractor: StrigaSignupInteractor,
) {
    suspend fun loadInitialAppData() {
        metadataInteractor.tryLoadAndSaveMetadata()
        strigaSignupInteractor.loadAndSaveSignupData()
        strigaUserInteractor.loadAndSaveUserStatusData()
    }

    suspend fun loadAllTokensDataIfEmpty() {
        userInteractor.loadAllTokensDataIfEmpty()
    }

    suspend fun loadUserTokensAndUpdateLocal(publicKey: PublicKey): List<Token.Active> {
        return userInteractor.loadUserTokensAndUpdateLocal(publicKey)
    }

    fun getHiddenTokensVisibility(): Boolean = userInteractor.getHiddenTokensVisibility()

    suspend fun getSingleTokenForBuy(): Token? = userInteractor.getSingleTokenForBuy()

    suspend fun getTokensForBuy(): List<Token> = userInteractor.getTokensForBuy()

    suspend fun findMultipleTokenData(tokenSymbols: List<String>): List<Token> =
        userInteractor.findMultipleTokenData(tokenSymbols)

    suspend fun setTokenHidden(mintAddress: String, visibility: String) {
        userInteractor.setTokenHidden(mintAddress, visibility)
    }

    suspend fun getUserTokens(): List<Token.Active> = userInteractor.getUserTokens()

    fun setHiddenTokensVisibility(isVisible: Boolean) = userInteractor.setHiddenTokensVisibility(isVisible)

    fun areZerosHidden(): Boolean = settingsInteractor.areZerosHidden()

    fun getUsername(): Username? = usernameInteractor.getUsername()

    fun isUsernameExist(): Boolean = usernameInteractor.isUsernameExist()

    suspend fun isSellFeatureAvailable(): Boolean = sellInteractor.isSellAvailable()

    fun setupEthereumKit(userSeedPhrase: List<String>) {
        ethereumInteractor.setup(userSeedPhrase)
    }

    fun getClaimBundleById(latestActiveBundleId: String): BridgeBundle? =
        ethereumInteractor.getClaimBundleById(latestActiveBundleId)

    suspend fun getClaimMinAmountForFreeFee(): BigDecimal =
        ethereumInteractor.getClaimMinAmountForFreeFee()

    fun getUserStatusBannerFlow(): StateFlow<StrigaKycStatusBanner?> = strigaUserInteractor.getUserStatusBannerFlow()
}
