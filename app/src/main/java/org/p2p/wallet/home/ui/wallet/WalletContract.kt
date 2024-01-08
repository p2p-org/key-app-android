package org.p2p.wallet.home.ui.wallet

import androidx.annotation.StringRes
import java.math.BigDecimal
import org.p2p.core.crypto.Base58String
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.home.ui.main.delegates.striga.offramp.StrigaOffRampCellModel
import org.p2p.wallet.home.ui.main.delegates.striga.onramp.StrigaOnRampCellModel
import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaBanner
import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId

interface WalletContract {

    interface View : MvpView {
        fun showBalance(fiatBalanceCellModel: TextViewCellModel?, tokenBalanceCellModel: TextViewCellModel?)
        fun showRefreshing(isRefreshing: Boolean)
        fun showStrigaOnRampProgress(isLoading: Boolean, tokenMint: Base58String)
        fun showStrigaOffRampProgress(isLoading: Boolean, accountId: StrigaAccountId)
        fun showStrigaBannerProgress(isLoading: Boolean)
        fun showUserAddress(ellipsizedAddress: String)
        fun showAddMoneyDialog()
        fun showKycPendingDialog()
        fun navigateToProfile()
        fun navigateToReserveUsername()
        fun navigateToStrigaOnRampConfirmOtp(
            challengeId: StrigaWithdrawalChallengeId,
            token: StrigaOnRampCellModel
        )
        fun navigateToStrigaOffRampConfirmOtp(
            challengeId: StrigaWithdrawalChallengeId,
            token: StrigaOffRampCellModel
        )
        fun navigateToStrigaByBanner(status: StrigaKycStatusBanner)
        fun showAddressCopied(addressOrUsername: String, @StringRes stringResId: Int)
        fun setCellItems(items: List<AnyCellItem>)
        fun showEmptyState(isEmpty: Boolean)
        fun setWithdrawButtonIsVisible(isVisible: Boolean)
        fun navigateToOffRamp()
        fun navigateToOffRampWithdrawEur(amountInEur: BigDecimal)
    }

    interface Presenter : MvpPresenter<View> {
        fun refreshTokens()
        fun onWithdrawClicked()
        fun onAddMoneyClicked()
        fun onProfileClick()
        fun onAddressClicked()
        fun onAmountClicked()
        fun onStrigaOnRampClicked(item: StrigaOnRampCellModel)
        fun onStrigaOffRampClicked(item: StrigaOffRampCellModel)
        fun onStrigaBannerClicked(item: StrigaBanner)
        fun onOnRampConfirmed(
            challengeId: StrigaWithdrawalChallengeId,
            token: StrigaOnRampCellModel
        )
        fun onOffRampConfirmed(
            challengeId: StrigaWithdrawalChallengeId,
            token: StrigaOffRampCellModel
        )

        fun onResume()
    }
}
