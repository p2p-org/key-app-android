package org.p2p.wallet.bridge.claim.ui.mapper

import android.content.res.Resources
import android.view.Gravity
import java.math.BigDecimal
import java.util.Date
import org.p2p.core.common.TextContainer
import org.p2p.core.model.TextHighlighting
import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.asPositiveUsdTransaction
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.isNullOrZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleMedium
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.bridge.claim.model.ClaimDetails
import org.p2p.wallet.bridge.claim.ui.model.ClaimScreenData
import org.p2p.wallet.bridge.model.BridgeAmount
import org.p2p.wallet.bridge.model.BridgeBundleFee
import org.p2p.wallet.bridge.model.BridgeBundleFees
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.utils.toPx

class ClaimUiMapper(private val resources: Resources) {

    fun prepareShowProgress(
        tokenToClaim: Token.Eth,
        claimDetails: ClaimDetails?
    ): NewShowProgress {
        val transactionDate = Date()
        val amountTokens = "${tokenToClaim.total.scaleMedium().formatToken()} ${tokenToClaim.tokenSymbol}"
        val amountUsd = tokenToClaim.totalInUsd.orZero()
        val feeList = listOfNotNull(
            claimDetails?.networkFee,
            claimDetails?.accountCreationFee,
            claimDetails?.bridgeFee
        )
        return NewShowProgress(
            date = transactionDate,
            tokenUrl = tokenToClaim.iconUrl.orEmpty(),
            amountTokens = amountTokens,
            amountUsd = amountUsd.asPositiveUsdTransaction(),
            recipient = null,
            totalFees = feeList.mapNotNull { it.toTextHighlighting() }
        )
    }

    fun makeClaimDetails(
        tokenToClaim: Token.Eth,
        fees: BridgeBundleFees,
        ethToken: TokenData?
    ): ClaimDetails {
        val tokenSymbol = tokenToClaim.tokenSymbol
        val decimals = tokenToClaim.decimals
        return ClaimDetails(
            willGetAmount = BridgeAmount(
                tokenSymbol,
                tokenToClaim.total,
                tokenToClaim.totalInUsd
            ),
            networkFee = ethToken?.let { ethTokenData ->
                fees.gasEth.toBridgeAmount(
                    tokenSymbol = ethTokenData.symbol,
                    decimals = ethTokenData.decimals
                )
            } ?: fees.gasEth.toBridgeAmount(tokenSymbol, decimals),
            accountCreationFee = fees.createAccount.toBridgeAmount(tokenSymbol, decimals),
            bridgeFee = fees.arbiterFee.toBridgeAmount(tokenSymbol, decimals)
        )
    }

    fun makeResultAmount(resultAmount: BridgeBundleFee, tokenToClaim: Token.Eth): BridgeAmount {
        return resultAmount.toBridgeAmount(tokenToClaim.tokenSymbol, tokenToClaim.decimals)
    }

    fun getTextSkeleton(): TextViewCellModel.Skeleton {
        return TextViewCellModel.Skeleton(
            SkeletonCellModel(
                height = 24.toPx(),
                width = 100.toPx(),
                radius = 4f.toPx(),
                gravity = Gravity.END
            )
        )
    }

    fun mapFeeTextContainer(fees: BridgeBundleFees, isFree: Boolean): TextViewCellModel.Raw {
        val feeList = listOf(fees.arbiterFee, fees.gasEth, fees.createAccount)
        val fee: BigDecimal = feeList.sumOf { it.amountInUsd?.toBigDecimal() ?: BigDecimal.ZERO }
        val feeValue = if (isFree) {
            resources.getString(R.string.bridge_claim_fees_free)
        } else {
            fee.asApproximateUsd(withBraces = false)
        }
        return TextViewCellModel.Raw(TextContainer(feeValue))
    }

    fun mapScreenData(tokenToClaim: Token.Eth): ClaimScreenData {
        return ClaimScreenData(
            title = resources.getString(R.string.bridge_claim_title_format, tokenToClaim.tokenSymbol),
            tokenIconUrl = tokenToClaim.iconUrl,
            tokenFormattedAmount = "${tokenToClaim.total.scaleMedium().formatToken()} ${tokenToClaim.tokenSymbol}",
            fiatFormattedAmount = tokenToClaim.totalInUsd.orZero().asApproximateUsd(withBraces = false),
        )
    }

    private fun BridgeBundleFee?.toBridgeAmount(
        tokenSymbol: String,
        decimals: Int,
    ): BridgeAmount {
        return BridgeAmount(
            tokenSymbol = tokenSymbol,
            tokenAmount = this?.amountInToken(decimals).takeIf { !it.isNullOrZero() },
            fiatAmount = this?.amountInUsd?.toBigDecimalOrZero()
        )
    }

    private fun BridgeAmount.toTextHighlighting(): TextHighlighting? {
        if (isFree) return null
        val usdText = formattedFiatAmount.orEmpty()
        val commonText = "$formattedTokenAmount $usdText"
        return TextHighlighting(
            commonText = commonText,
            highlightedText = usdText
        )
    }
}