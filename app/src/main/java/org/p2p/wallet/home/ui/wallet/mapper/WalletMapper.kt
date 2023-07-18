package org.p2p.wallet.home.ui.wallet.mapper

import android.content.res.Resources
import android.view.Gravity
import java.math.BigDecimal
import org.p2p.core.common.TextContainer
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.utils.Constants
import org.p2p.core.utils.formatFiat
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.home.ui.main.delegates.striga.onramp.StrigaOnRampCellModel
import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaBanner
import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.onramp.interactor.StrigaOnRampToken
import org.p2p.wallet.utils.toPx

class WalletMapper(private val resources: Resources) {

    fun mapFiatBalance(balance: BigDecimal): TextViewCellModel {
        val result = resources.getString(R.string.home_usd_format, balance.formatFiat())
        return TextViewCellModel.Raw(TextContainer(result))
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

    fun mapTokenBalance(balance: BigDecimal): TextViewCellModel {
        val result = resources.getString(R.string.wallet_token_format, balance.formatFiat(), Constants.USDC_SYMBOL)
        return TextViewCellModel.Raw(TextContainer(result))
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
                    tokenName = it.tokenDetails.tokenName,
                    tokenMintAddress = mintAddress,
                    tokenSymbol = it.tokenDetails.tokenSymbol,
                    tokenIcon = it.tokenDetails.iconUrl.orEmpty(),
                    isLoading = false,
                    payload = it
                )
            }
            return this
        }

        fun build(): List<AnyCellItem> = cellItems
    }
}
