package org.p2p.wallet.home.ui.main

import java.math.BigDecimal
import kotlinx.coroutines.flow.SharedFlow
import org.p2p.core.token.Token
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.onramp.interactor.StrigaOnRampInteractor
import org.p2p.wallet.striga.onramp.interactor.StrigaOnRampToken
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId
import org.p2p.wallet.user.interactor.UserInteractor

class HomeInteractor(
    private val userInteractor: UserInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val usernameInteractor: UsernameInteractor,
    private val sellInteractor: SellInteractor,
    private val ethereumInteractor: EthereumInteractor,
    private val strigaUserInteractor: StrigaUserInteractor,
    private val strigaOnRampInteractor: StrigaOnRampInteractor,
    private val strigaWalletInteractor: StrigaWalletInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    @Deprecated("To be removed when striga is removed from the HomePresenter")
    suspend fun loadDetailsForStrigaAccounts(): Result<Unit> =
        strigaWalletInteractor.loadDetailsForStrigaAccounts()

    fun getHiddenTokensVisibility(): Boolean = userInteractor.getHiddenTokensVisibility()

    suspend fun getSingleTokenForBuy(): Token? = userInteractor.getSingleTokenForBuy()

    suspend fun getTokensForBuy(): List<Token> = userInteractor.getTokensForBuy()

    suspend fun findMultipleTokenData(tokenSymbols: List<String>): List<Token.Other> =
        userInteractor.findMultipleTokenData(tokenSymbols)

    suspend fun setTokenHidden(mintAddress: String, visibility: String) {
        userInteractor.setTokenHidden(mintAddress, visibility)
    }

    fun setHiddenTokensVisibility(isVisible: Boolean) = userInteractor.setHiddenTokensVisibility(isVisible)

    fun areZerosHidden(): Boolean = settingsInteractor.areZerosHidden()

    fun getUsername(): Username? = usernameInteractor.getUsername()

    fun isUsernameExist(): Boolean = usernameInteractor.isUsernameExist()

    suspend fun isSellFeatureAvailable(): Boolean = sellInteractor.isSellAvailable()

    fun getClaimBundleById(latestActiveBundleId: String): BridgeBundle? =
        ethereumInteractor.getClaimBundleById(latestActiveBundleId)

    suspend fun getClaimMinAmountForFreeFee(): BigDecimal =
        ethereumInteractor.getClaimMinAmountForFreeFee()

    @Deprecated("To be removed when striga is removed from the HomePresenter")
    fun getUserStatusBannerFlow(): SharedFlow<StrigaKycStatusBanner?> = strigaUserInteractor.getUserStatusBannerFlow()

    @Deprecated("To be removed when striga is removed from the HomePresenter")
    fun hideStrigaUserStatusBanner(banner: StrigaKycStatusBanner) = strigaUserInteractor.hideUserStatusBanner(banner)

    @Deprecated("To be removed when striga is removed from the HomePresenter")
    suspend fun claimStrigaToken(
        amountLamports: BigDecimal,
        token: StrigaOnRampToken
    ): StrigaDataLayerResult<StrigaWithdrawalChallengeId> {
        return strigaOnRampInteractor.onRampToken(amountLamports, token)
    }
}
