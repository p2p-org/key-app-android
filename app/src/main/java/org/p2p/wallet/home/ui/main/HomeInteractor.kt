package org.p2p.wallet.home.ui.main

import java.math.BigDecimal
import kotlinx.coroutines.flow.StateFlow
import org.p2p.core.token.Token
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.home.events.HomeScreenStateLoader
import org.p2p.wallet.home.repository.HomeScreenLocalRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.wallet.interactor.StrigaClaimInteractor
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor
import org.p2p.wallet.striga.wallet.models.StrigaClaimableToken
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.user.interactor.UserTokensInteractor

class HomeInteractor(
    private val userInteractor: UserInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val usernameInteractor: UsernameInteractor,
    private val sellInteractor: SellInteractor,
    private val ethereumInteractor: EthereumInteractor,
    private val strigaUserInteractor: StrigaUserInteractor,
    private val strigaClaimInteractor: StrigaClaimInteractor,
    private val strigaWalletInteractor: StrigaWalletInteractor,
    private val userTokensInteractor: UserTokensInteractor,
    private val homeScreenLocalRepository: HomeScreenLocalRepository,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun loadStrigaFiatAccountDetails(): Result<StrigaFiatAccountDetails> {
        return strigaWalletInteractor.loadFiatAccountAndUserWallet()
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

    fun getClaimBundleById(latestActiveBundleId: String): BridgeBundle? =
        ethereumInteractor.getClaimBundleById(latestActiveBundleId)

    suspend fun getClaimMinAmountForFreeFee(): BigDecimal =
        ethereumInteractor.getClaimMinAmountForFreeFee()

    fun getUserStatusBannerFlow(): StateFlow<StrigaKycStatusBanner?> = strigaUserInteractor.getUserStatusBannerFlow()

    fun hideStrigaUserStatusBanner(banner: StrigaKycStatusBanner) = strigaUserInteractor.hideUserStatusBanner(banner)

    suspend fun claimStrigaToken(
        amountLamports: BigDecimal,
        token: StrigaClaimableToken
    ): StrigaDataLayerResult<StrigaWithdrawalChallengeId> {
        return strigaClaimInteractor.claim(amountLamports, token)
    }

    fun loadUserRates(tokens: List<Token.Active>) {
        userTokensInteractor.loadUserRates(tokens)
    }

    suspend fun updateHomeScreenState(newTokensState: HomeScreenStateLoader.HomeScreenState) {
        homeScreenLocalRepository.setUserTokensState(newTokensState)
    }

    fun observeHomeScreenState(): StateFlow<HomeScreenStateLoader.HomeScreenState?> =
        homeScreenLocalRepository.getUserTokensStateFlow()

    suspend fun updateRefreshState(isRefreshing: Boolean) {
        homeScreenLocalRepository.setRefreshState(isRefreshing)
    }

    fun observeRefreshState(): StateFlow<Boolean> {
        return homeScreenLocalRepository.getHomeScreenRefreshStateFlow()
    }

    suspend fun updateHomeActionButtons(newButtons: List<ActionButton>) {
        homeScreenLocalRepository.setActionButtons(newButtons)
    }

    fun observeActionButtons(): StateFlow<List<ActionButton>> {
        return homeScreenLocalRepository.getHomeScreenActionButtonsFlow()
    }

    fun observeStrigaKycBanner(): StateFlow<StrigaKycStatusBanner?> {
        return homeScreenLocalRepository.getStrigaUserStatusBannerFlow()
    }

    suspend fun updateStrigaKycBanner(banner: StrigaKycStatusBanner) {
        homeScreenLocalRepository.setStrigaUserStatusBanner(banner)
    }

    suspend fun updateUsername(username: String) {
        homeScreenLocalRepository.setUsername(username)
    }

    fun observeUsername(): StateFlow<String?> {
        return homeScreenLocalRepository.getUsernameFlow()
    }

    suspend fun getUsernameOrPublicAddress(): String {
        return tokenKeyProvider.publicKey
    }

    suspend fun updateUserBalance(userBalance: BigDecimal?) {
        homeScreenLocalRepository.setUserBalance(userBalance)
    }

    fun observeUserBalance(): StateFlow<BigDecimal?> {
        return homeScreenLocalRepository.observeUserBalance()
    }
}
