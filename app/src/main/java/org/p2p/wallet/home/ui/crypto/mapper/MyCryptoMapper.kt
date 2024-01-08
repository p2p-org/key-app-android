package org.p2p.wallet.home.ui.crypto.mapper

import androidx.annotation.ColorRes
import android.content.res.Resources
import java.math.BigDecimal
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.core.utils.formatFiat
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.bridge.claim.model.ClaimDetails
import org.p2p.wallet.bridge.claim.ui.mapper.ClaimUiMapper
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.home.model.VisibilityState
import org.p2p.wallet.home.ui.main.delegates.bridgeclaim.EthClaimTokenCellModel
import org.p2p.wallet.home.ui.main.delegates.hidebutton.TokenButtonCellModel
import org.p2p.wallet.home.ui.main.delegates.token.TokenCellModel
import org.p2p.wallet.transaction.model.NewShowProgress

class MyCryptoMapper(
    private val resources: Resources,
    private val claimUiMapper: ClaimUiMapper,
) {

    fun mapBalance(balance: BigDecimal): TextViewCellModel {
        val result = resources.getString(R.string.home_usd_format, balance.formatFiat())
        return TextViewCellModel.Raw(TextContainer(result))
    }

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

    fun mapToCellItems(
        tokens: List<Token.Active>,
        ethereumTokens: List<Token.Eth>,
        visibilityState: VisibilityState,
        isZerosHidden: Boolean
    ): List<AnyCellItem> {
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
            result += hiddenTokens.map { it.mapToCellModel(isZerosHidden) }
        }

        return result.toList()
    }

    private fun Token.Active.mapToCellModel(isZerosHidden: Boolean): TokenCellModel {
        return TokenCellModel(
            iconUrl = iconUrl,
            tokenName = tokenName,
            isWrapped = isWrapped,
            formattedUsdTotal = getFormattedUsdTotal(),
            formattedTotal = getFormattedTotal(includeSymbol = true),
            isDefinitelyHidden = isDefinitelyHidden(isZerosHidden),
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
