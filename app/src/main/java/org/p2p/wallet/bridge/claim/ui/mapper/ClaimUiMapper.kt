package org.p2p.wallet.bridge.claim.ui.mapper

import android.content.res.Resources
import android.view.Gravity
import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal
import org.p2p.core.common.TextContainer
import org.p2p.core.model.TextHighlighting
import org.p2p.core.token.Token
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.asPositiveUsdTransaction
import org.p2p.core.utils.orZero
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.bridge.claim.model.ClaimDetails
import org.p2p.wallet.bridge.claim.ui.model.ClaimScreenData
import org.p2p.wallet.bridge.model.BridgeAmount
import org.p2p.wallet.bridge.model.BridgeBundleFees
import org.p2p.wallet.bridge.model.BridgeFee
import org.p2p.wallet.bridge.model.toBridgeAmount
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.utils.toPx

class ClaimUiMapper(private val resources: Resources) {

    fun prepareShowProgress(
        amountToClaim: BigDecimal,
        iconUrl: String,
        claimDetails: ClaimDetails?
    ): NewShowProgress {
        val transactionDate = claimDetails?.transactionDate ?: ZonedDateTime.now()
        val willGetAmount = claimDetails?.willGetAmount
        val amountTokens = willGetAmount?.formattedTokenAmount.orEmpty()
        val amountUsd = willGetAmount?.fiatAmount.orZero()
        val minAmountForFreeFee = claimDetails?.minAmountForFreeFee.orZero()
        val isFreeTransaction = amountToClaim >= minAmountForFreeFee
        val feeList = claimDetails?.let {
            listOf(it.networkFee, it.accountCreationFee, it.bridgeFee)
        }?.filter { !isFreeTransaction }?.ifEmpty { null }
        return NewShowProgress(
            date = transactionDate,
            tokenUrl = iconUrl,
            amountTokens = amountTokens,
            amountUsd = amountUsd.asPositiveUsdTransaction(),
            recipient = null,
            totalFees = feeList?.let { listOf(toTextHighlighting(feeList)) },
            amountColor = R.color.text_mint
        )
    }

    fun makeClaimDetails(
        isFree: Boolean,
        resultAmount: BridgeFee,
        fees: BridgeBundleFees?,
        minAmountForFreeFee: BigDecimal,
        transactionDate: ZonedDateTime,
    ): ClaimDetails {
        val defaultFee = fees?.gasFeeInToken.toBridgeAmount()
        return ClaimDetails(
            isFree = isFree,
            willGetAmount = resultAmount.toBridgeAmount(),
            networkFee = defaultFee,
            accountCreationFee = fees?.createAccount.toBridgeAmount(),
            bridgeFee = fees?.arbiterFee.toBridgeAmount(),
            minAmountForFreeFee = minAmountForFreeFee,
            transactionDate = transactionDate
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
        val feeList = listOf(fees.arbiterFee, fees.gasFeeInToken, fees.createAccount)
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
            tokenFormattedAmount = tokenToClaim.getFormattedTotal(includeSymbol = true),
            fiatFormattedAmount = tokenToClaim.totalInUsd.orZero().asApproximateUsd(withBraces = false),
        )
    }

    private fun toTextHighlighting(items: List<BridgeAmount>): TextHighlighting {
        val usdText = items.filter { !it.isZero }.sumOf { it.fiatAmount.orZero() }.asApproximateUsd(withBraces = false)
        return TextHighlighting(
            commonText = usdText,
            highlightedText = usdText
        )
    }
}
