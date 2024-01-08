package org.p2p.wallet.home.ui.wallet.mapper

import android.view.Gravity
import java.math.BigDecimal
import org.p2p.core.common.TextContainer
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.core.utils.asCurrencyAfter
import org.p2p.core.utils.asUsd
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.home.ui.main.delegates.striga.offramp.StrigaOffRampCellModel
import org.p2p.wallet.home.ui.main.delegates.striga.onramp.StrigaOnRampCellModel
import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaBanner
import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.offramp.models.StrigaOffRampToken
import org.p2p.wallet.striga.onramp.interactor.StrigaOnRampToken
import org.p2p.wallet.utils.toPx

class WalletMapper() {

    fun mapFiatBalance(balance: BigDecimal): TextViewCellModel {
        return TextViewCellModel.Raw(
            TextContainer.Raw(balance.asUsd())
        )
    }

    fun getFiatBalanceSkeleton(): TextViewCellModel.Skeleton =
        TextViewCellModel.Skeleton(
            SkeletonCellModel(
                height = 64.toPx(),
                width = 262.toPx(),
                radius = 4f.toPx(),
                gravity = Gravity.CENTER
            )
        )

    fun mapTokenBalance(balanceToken: Token.Active?): TextViewCellModel {
        val formattedBalance = balanceToken?.getFormattedTotal(true)
            ?: BigDecimal.ZERO.asCurrencyAfter(Constants.USDC_SYMBOL)
        return TextViewCellModel.Raw(
            TextContainer.Raw(formattedBalance)
        )
    }

    fun getTokenBalanceSkeleton(): TextViewCellModel.Skeleton =
        TextViewCellModel.Skeleton(
            SkeletonCellModel(
                height = 20.toPx(),
                width = 94.toPx(),
                radius = 4f.toPx(),
                gravity = Gravity.CENTER
            )
        )

    fun buildCellItems(mapper: Builder.() -> Unit): List<AnyCellItem> {
        return Builder().apply(mapper).build()
    }

    inner class Builder internal constructor() {
        private val cellItems = mutableListOf<AnyCellItem>()

        fun mapStrigaKycBanner(banner: StrigaKycStatusBanner?): Builder = apply {
            banner?.let {
                cellItems += StrigaBanner(isLoading = false, status = banner)
            }
        }

        fun mapStrigaOnRampTokens(strigaOnRampTokens: List<StrigaOnRampToken>): Builder {
            cellItems += strigaOnRampTokens.map {
                val mintAddress = it.tokenDetails.mintAddress.toBase58Instance()
                StrigaOnRampCellModel(
                    amountAvailable = it.claimableAmount,
                    tokenMintAddress = mintAddress,
                    tokenSymbol = it.tokenDetails.tokenSymbol,
                    tokenIcon = it.tokenDetails.iconUrl.orEmpty(),
                    isLoading = false,
                    payload = it
                )
            }
            return this
        }

        fun mapStrigaOffRampTokens(strigaOffRampTokens: List<StrigaOffRampToken>): Builder {
            cellItems += strigaOffRampTokens.map {
                StrigaOffRampCellModel(
                    amountAvailable = it.amountToWithdraw,
                    tokenSymbol = Constants.EUR_SYMBOL,
                    isLoading = false,
                    payload = it
                )
            }
            return this
        }

        fun build(): List<AnyCellItem> = cellItems
    }
}
