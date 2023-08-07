package org.p2p.wallet.home.ui.wallet.interactor

import kotlinx.coroutines.flow.StateFlow
import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaKycStatusBanner
import org.p2p.wallet.home.ui.wallet.model.WalletStrigaOnOffRampTokens
import org.p2p.wallet.home.ui.wallet.repository.WalletStrigaOnOffRampTokensRepository
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor

class WalletStrigaInteractor(
    private val onOffRampTokensRepository: WalletStrigaOnOffRampTokensRepository,
    private val strigaUserInteractor: StrigaUserInteractor,
) {
    suspend fun loadOnOffRampTokens() = onOffRampTokensRepository.load()
    fun observeOnOffRampTokens(): StateFlow<WalletStrigaOnOffRampTokens> = onOffRampTokensRepository.observeTokens()
    fun observeKycBanner(): StateFlow<StrigaKycStatusBanner?> = strigaUserInteractor.getUserStatusBannerFlow()
}
