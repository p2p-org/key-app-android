package org.p2p.wallet.home.ui.wallet

import org.p2p.core.crypto.Base58String
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.home.ui.main.delegates.striga.onramp.StrigaOnRampCellModel
import org.p2p.wallet.kyc.model.StrigaBanner
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId

interface WalletContract {

    interface View : MvpView {
        fun showBalance(cellModel: TextViewCellModel?)
        fun showRefreshing(isRefreshing: Boolean)
        fun showStrigaOnRampProgress(isLoading: Boolean, tokenMint: Base58String)
        fun showStrigaBannerProgress(isLoading: Boolean)
        fun showUserAddress(ellipsizedAddress: String)
        fun showActionButtons(buttons: List<ActionButton>)
        fun showTopupWalletDialog()
        fun showKycPendingDialog()
        fun navigateToProfile()
        fun navigateToReserveUsername()
        fun navigateToStrigaOnRampConfirmOtp(
            challengeId: StrigaWithdrawalChallengeId,
            token: StrigaOnRampCellModel
        )
        fun navigateToStrigaByBanner(status: StrigaKycStatusBanner)
        fun showAddressCopied(addressOrUsername: String)
        fun setCellItems(items: List<AnyCellItem>)
    }

    interface Presenter : MvpPresenter<View> {
        fun refreshTokens()
        fun onSellClicked()
        fun onTopupClicked()
        fun onProfileClick()
        fun onAddressClicked()
        fun onStrigaOnRampClicked(item: StrigaOnRampCellModel)
        fun onStrigaBannerClicked(item: StrigaBanner)
        fun onOnRampConfirmed(
            challengeId: StrigaWithdrawalChallengeId,
            token: StrigaOnRampCellModel
        )
    }
}
