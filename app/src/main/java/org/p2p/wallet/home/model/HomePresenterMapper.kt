package org.p2p.wallet.home.model

import androidx.annotation.ColorRes
import android.content.res.Resources
import java.math.BigDecimal
import kotlinx.coroutines.withContext
import org.p2p.core.common.TextContainer
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token
import org.p2p.core.utils.formatFiat
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.bridge.claim.model.ClaimDetails
import org.p2p.wallet.bridge.claim.ui.mapper.ClaimUiMapper
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.home.ui.main.delegates.bridgeclaim.EthClaimTokenCellModel
import org.p2p.wallet.home.ui.main.delegates.hidebutton.TokenButtonCellModel
import org.p2p.wallet.home.ui.main.delegates.token.TokenCellModel
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.kyc.model.StrigaKycUiBannerMapper
import org.p2p.wallet.striga.wallet.models.StrigaClaimableToken
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
        strigaClaimableTokens: List<StrigaClaimableToken>,
        visibilityState: VisibilityState,
        isZerosHidden: Boolean
    ): List<HomeElementItem> {
        // TODO remove after striga will move to WS
        return withContext(dispatchers.io) {

            val isHiddenGroupToTokens: Map<Boolean, List<Token.Active>> = tokens.groupBy { token ->
                token.isDefinitelyHidden(isZerosHidden)
            }

            val hiddenTokens = isHiddenGroupToTokens[true].orEmpty()
            val visibleTokens = isHiddenGroupToTokens[false].orEmpty()

            val result = mutableListOf<HomeElementItem>()

            result += HomeElementItem.Title(R.string.home_tokens)

            result += strigaClaimableTokens.map {
                val mintAddress = it.tokenDetails.mintAddress.toBase58Instance()
                HomeElementItem.StrigaClaim(
                    strigaToken = it,
                    amountAvailable = it.claimableAmount,
                    tokenName = it.tokenDetails.tokenName,
                    tokenMintAddress = mintAddress,
                    tokenSymbol = it.tokenDetails.tokenSymbol,
                    tokenIcon = it.tokenDetails.iconUrl.orEmpty(),
                    isClaimInProcess = false
                )
            }

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

    suspend fun mapToCellItems(
        tokens: List<Token.Active>,
        ethereumTokens: List<Token.Eth>,
        strigaClaimableTokens: List<StrigaClaimableToken>,
        visibilityState: VisibilityState,
        isZerosHidden: Boolean
    ): List<AnyCellItem> {
        return withContext(dispatchers.io) {

            val isHiddenGroupToTokens: Map<Boolean, List<Token.Active>> = tokens.groupBy { token ->
                token.isDefinitelyHidden(isZerosHidden)
            }

            val hiddenTokens = isHiddenGroupToTokens[true].orEmpty()
            val visibleTokens = isHiddenGroupToTokens[false].orEmpty()

            val result = mutableListOf<AnyCellItem>()

            result += ethereumTokens.map { it.mapToCellModel() }
            result += visibleTokens.map { it.mapToCellModel(isZerosHidden) }

            if (hiddenTokens.isNotEmpty()) {
                val isHidden = visibilityState is VisibilityState.Hidden
                val iconResId = if (isHidden) R.drawable.ic_token_expose else R.drawable.ic_token_hide
                result += TokenButtonCellModel(visibilityIcon = iconResId, payload = visibilityState)
            }

            if (visibilityState.isVisible) {
                result += hiddenTokens.map { it.mapToCellModel(!isZerosHidden) }
            }

            result.toList()
        }
    }

    private fun Token.Active.mapToCellModel(isZerosHidden: Boolean): TokenCellModel {
        return TokenCellModel(
            iconUrl = iconUrl,
            tokenName = tokenName,
            isWrapped = isWrapped,
            formattedUsdTotal = getFormattedUsdTotal(),
            formattedTotal = getFormattedTotal(includeSymbol = true),
            visibilityIcon = getVisibilityIcon(isZerosHidden),
            payload = this
        )
    }

    private fun Token.Eth.mapToCellModel(): EthClaimTokenCellModel {
        val isClaimEnabled = !isClaiming
        val buttonText: String
        @ColorRes val buttonTextColor: Int
        @ColorRes val buttonBackgroundColor: Int
        if (isClaimEnabled) {
            buttonText = resources.getString(R.string.bridge_claim_button_text)
            buttonTextColor = R.color.text_snow
            buttonBackgroundColor = R.color.bg_night
        } else {
            buttonText = resources.getString(R.string.bridge_claiming_button_text)
            buttonTextColor = R.color.text_mountain
            buttonBackgroundColor = R.color.bg_rain
        }
        return EthClaimTokenCellModel(
            iconUrl = iconUrl,
            tokenName = tokenName,
            formattedTotal = getFormattedTotal(includeSymbol = true),
            buttonText = buttonText,
            buttonTextColor = buttonTextColor,
            buttonBackgroundColor = buttonBackgroundColor,
            isClaimEnabled = isClaimEnabled,
            payload = this
        )
    }
}
