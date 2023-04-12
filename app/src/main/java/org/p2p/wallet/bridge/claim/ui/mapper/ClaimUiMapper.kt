package org.p2p.wallet.bridge.claim.ui.mapper

import android.content.res.Resources
import android.view.Gravity
import java.math.BigDecimal
import java.util.Date
import org.p2p.core.common.TextContainer
import org.p2p.core.model.TextHighlighting
import org.p2p.core.token.Token
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.asPositiveUsdTransaction
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.isNullOrZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.bridge.claim.model.ClaimDetails
import org.p2p.wallet.bridge.claim.ui.model.ClaimScreenData
import org.p2p.wallet.bridge.model.BridgeAmount
import org.p2p.wallet.bridge.model.BridgeBundleFees
import org.p2p.wallet.bridge.model.BridgeFee
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.utils.toPx

private const val CLAIM_TOKEN_AMOUNT_SCALE = 8

class ClaimUiMapper(private val resources: Resources) {

    fun prepareShowProgress(
        tokenToClaim: Token.Eth,
        claimDetails: ClaimDetails?
    ): NewShowProgress {
        val transactionDate = Date()
        val willGetAmount = claimDetails?.willGetAmount
        val amountTokens = willGetAmount?.formattedTokenAmount.orEmpty()
        val amountUsd = willGetAmount?.fiatAmount.orZero()
        val amountToClaim = tokenToClaim.total
        val minAmountForFreeFee = claimDetails?.minAmountForFreeFee.orZero()
        val isFreeTransaction = amountToClaim >= minAmountForFreeFee
        val feeList = claimDetails?.let {
            listOf(it.networkFee, it.accountCreationFee, it.bridgeFee)
        }?.filter { !it.isFree && !isFreeTransaction }?.ifEmpty { null }
        return NewShowProgress(
            date = transactionDate,
            tokenUrl = tokenToClaim.iconUrl.orEmpty(),
            amountTokens = amountTokens,
            amountUsd = amountUsd.asPositiveUsdTransaction(),
            recipient = null,
            totalFees = feeList?.let { listOf(toTextHighlighting(feeList)) }
        )
    }

    fun makeClaimDetails(
        tokenToClaim: Token.Eth,
        resultAmount: BridgeFee,
        fees: BridgeBundleFees?,
        minAmountForFreeFee: BigDecimal
    ): ClaimDetails {
        val defaultFee = fees?.gasEth.toBridgeAmount()
        return ClaimDetails(
            willGetAmount = resultAmount.toBridgeAmount(),
            networkFee = defaultFee,
            accountCreationFee = fees?.createAccount.toBridgeAmount(),
            bridgeFee = fees?.arbiterFee.toBridgeAmount(),
            minAmountForFreeFee = minAmountForFreeFee,
            claimAmount = tokenToClaim.total
        )
    }

    fun makeResultAmount(resultAmount: BridgeFee): BridgeAmount {
        return resultAmount.toBridgeAmount()
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
            tokenFormattedAmount = "${
                tokenToClaim.total.scaleLong(CLAIM_TOKEN_AMOUNT_SCALE).formatToken(tokenToClaim.decimals)
            } ${tokenToClaim.tokenSymbol}",
            fiatFormattedAmount = tokenToClaim.totalInUsd.orZero().asApproximateUsd(withBraces = false),
        )
    }

    private fun BridgeFee?.toBridgeAmount(): BridgeAmount {
        return BridgeAmount(
            tokenSymbol = this?.symbol.orEmpty(),
            tokenDecimals = this?.decimals.orZero(),
            tokenAmount = this?.amountInToken?.takeIf { !it.isNullOrZero() },
            fiatAmount = this?.amountInUsd?.toBigDecimalOrZero()
        )
    }

    private fun toTextHighlighting(items: List<BridgeAmount>): TextHighlighting {
        val usdText = items.filter { !it.isFree }.sumOf { it.fiatAmount.orZero() }.asApproximateUsd(withBraces = false)
        return TextHighlighting(
            commonText = usdText,
            highlightedText = usdText
        )
    }
}
