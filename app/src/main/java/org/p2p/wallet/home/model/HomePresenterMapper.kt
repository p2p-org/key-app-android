package org.p2p.wallet.home.model

import android.content.res.Resources
import java.math.BigDecimal
import kotlinx.coroutines.withContext
import org.p2p.core.common.TextContainer
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token
import org.p2p.core.utils.formatFiat
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.bridge.claim.model.ClaimDetails
import org.p2p.wallet.bridge.claim.ui.mapper.ClaimUiMapper
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.kyc.model.StrigaKycUiBannerMapper
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.utils.toPx

class HomePresenterMapper(
    private val resources: Resources,
    private val claimUiMapper: ClaimUiMapper,
    private val strigaUiBannerMapper: StrigaKycUiBannerMapper,
    private val dispatchers: CoroutineDispatchers
) {

    fun mapBalance(balance: BigDecimal): TextViewCellModel {
        val result = resources.getString(R.string.home_usd_format, balance.formatFiat())
        return TextViewCellModel.Raw(TextContainer(result))
    }

    fun mapRateSkeleton(): TextViewCellModel =
        TextViewCellModel.Skeleton(
            SkeletonCellModel(
                height = 34.toPx(),
                width = 160.toPx(),
                radius = 4f.toPx()
            )
        )

    fun mapToClaimDetails(bridgeBundle: BridgeBundle, minAmountForFreeFee: BigDecimal): ClaimDetails {
        return claimUiMapper.makeClaimDetails(bridgeBundle, minAmountForFreeFee)
    }

    fun mapShowProgressForClaim(
        amountToClaim: BigDecimal,
        iconUrl: String,
        claimDetails: ClaimDetails
    ): NewShowProgress {
        return claimUiMapper.prepareShowProgress(amountToClaim, iconUrl, claimDetails)
    }

    fun getKycStatusBannerFromTitle(bannerTitleId: Int): StrigaKycStatusBanner? {
        return strigaUiBannerMapper.getKycStatusBannerFromTitle(bannerTitleId)
    }

    fun mapToBigBanner(banner: StrigaKycStatusBanner, isLoading: Boolean): HomeBannerItem {
        return strigaUiBannerMapper.mapToHomeBigBanner(banner, isLoading)
    }

    fun mapToHomeBanner(isLoading: Boolean, banner: StrigaKycStatusBanner): HomeScreenBanner {
        return strigaUiBannerMapper.mapToBanner(isLoading, banner)
    }

    suspend fun mapToItems(
        tokens: List<Token.Active>,
        ethereumTokens: List<Token.Eth>,
        visibilityState: VisibilityState,
        isZerosHidden: Boolean
    ): List<HomeElementItem> {
        return withContext(dispatchers.io) {

            val groups: Map<Boolean, List<Token.Active>> = tokens.groupBy { token ->
                token.isDefinitelyHidden(isZerosHidden)
            }

            val hiddenTokens = groups[true].orEmpty()
            val visibleTokens = groups[false].orEmpty()

            val result = mutableListOf<HomeElementItem>(HomeElementItem.Title(R.string.home_tokens))

            result += ethereumTokens.map { token ->
                HomeElementItem.Claim(
                    token = token,
                    isClaimEnabled = !token.isClaiming
                )
            }

            result += visibleTokens.map { HomeElementItem.Shown(it) }

            if (hiddenTokens.isNotEmpty()) {
                result += HomeElementItem.Action(visibilityState)
            }

            if (visibilityState.isVisible) {
                result += hiddenTokens.map { HomeElementItem.Hidden(it, visibilityState) }
            }

            result.toList()
        }
    }
}
