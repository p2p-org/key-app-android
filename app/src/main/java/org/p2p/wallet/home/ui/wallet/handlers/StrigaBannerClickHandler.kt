package org.p2p.wallet.home.ui.wallet.handlers

import org.p2p.wallet.R
import org.p2p.wallet.home.ui.wallet.WalletContract
import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaBanner
import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaKycStatusBanner
import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaKycUiBannerMapper
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor

class StrigaBannerClickHandler(
    private val strigaKycUiBannerMapper: StrigaKycUiBannerMapper,
    private val strigaUserInteractor: StrigaUserInteractor,
    private val strigaWalletInteractor: StrigaWalletInteractor,
) {

    suspend fun handle(view: WalletContract.View?, item: StrigaBanner) {
        with(item.status) {
            val statusFromKycBanner = strigaKycUiBannerMapper.getKycStatusBannerFromTitle(bannerTitleResId)
            when {
                statusFromKycBanner == StrigaKycStatusBanner.PENDING -> {
                    view?.showKycPendingDialog()
                }
                statusFromKycBanner != null -> {
                    // hide banner if necessary
                    strigaUserInteractor.hideUserStatusBanner(statusFromKycBanner)

                    if (statusFromKycBanner == StrigaKycStatusBanner.VERIFICATION_DONE) {
                        view?.showStrigaBannerProgress(isLoading = true)
                        strigaWalletInteractor.loadDetailsForStrigaAccounts()
                            .onSuccess { view?.navigateToStrigaByBanner(statusFromKycBanner) }
                            .onFailure { view?.showUiKitSnackBar(messageResId = R.string.error_general_message) }
                        view?.showStrigaBannerProgress(isLoading = false)
                    } else {
                        view?.navigateToStrigaByBanner(statusFromKycBanner)
                    }
                }
                else -> {
                    view?.showTopupWalletDialog()
                }
            }
        }
    }
}
