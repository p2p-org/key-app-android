package org.p2p.wallet.striga.offramp.mappers

import java.math.BigDecimal
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.STRIGA_FIAT_DECIMALS
import org.p2p.core.utils.emptyString
import org.p2p.core.utils.formatToken
import org.p2p.uikit.utils.skeleton.textCellSkeleton
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.jupiter.ui.main.widget.SwapWidgetModel
import org.p2p.wallet.striga.offramp.models.StrigaOffRampTokenState
import org.p2p.wallet.striga.offramp.models.StrigaOffRampTokenType

class StrigaOffRampSwapWidgetMapper {

    fun mapByState(tokenType: StrigaOffRampTokenType, state: StrigaOffRampTokenState): SwapWidgetModel {
        return when (tokenType) {
            StrigaOffRampTokenType.TokenA -> {
                mapStateToSwapWidgetModel(
                    titleResId = tokenType.titleResId,
                    currencyName = tokenType.currencyName,
                    amount = state.amount,
                    balance = state.balance,
                    isLoadingAmount = state is StrigaOffRampTokenState.Loading,
                    isLoadingBalance = state is StrigaOffRampTokenState.LoadingBalance,
                    isStatic = state is StrigaOffRampTokenState.Disabled
                )
            }
            StrigaOffRampTokenType.TokenB -> {
                mapStateToSwapWidgetModel(
                    titleResId = tokenType.titleResId,
                    currencyName = tokenType.currencyName,
                    amount = state.amount,
                    balance = null,
                    isLoadingAmount = state is StrigaOffRampTokenState.Loading,
                    isLoadingBalance = state is StrigaOffRampTokenState.LoadingBalance,
                    isStatic = state is StrigaOffRampTokenState.Disabled
                )
            }
        }
    }

    fun mapTokenA(
        amount: BigDecimal,
        balance: BigDecimal
    ): StrigaOffRampTokenState {
        return StrigaOffRampTokenState.Content(
            amount = amount,
            balance = balance
        )
    }

    fun mapTokenALoadingBalance(amountA: BigDecimal): StrigaOffRampTokenState {
        return StrigaOffRampTokenState.LoadingBalance(
            amountA = amountA,
        )
    }

    fun mapTokenB(amount: BigDecimal): StrigaOffRampTokenState = StrigaOffRampTokenState.Content(amount)

    private fun mapStateToSwapWidgetModel(
        titleResId: Int,
        currencyName: String,
        amount: BigDecimal,
        balance: BigDecimal? = null,
        isLoadingAmount: Boolean = false,
        isLoadingBalance: Boolean = false,
        isStatic: Boolean = false
    ): SwapWidgetModel {
        return SwapWidgetModel.Content(
            isStatic = isStatic,
            widgetTitle = mapWidgetTitle(titleResId),
            availableAmount = mapAvailableAmount(isLoadingBalance, balance, currencyName),
            currencyName = currencyName.toRawTextViewCellModel(),
            amount = mapAmount(isLoadingAmount, amount),
            amountMaxDecimals = STRIGA_FIAT_DECIMALS,
            // not used in striga
            balance = emptyString().toRawTextViewCellModel(),
            fiatAmount = null
        )
    }

    private fun mapAvailableAmount(
        isLoadingBalance: Boolean,
        balance: BigDecimal?,
        currencyName: String
    ): TextViewCellModel? {
        return if (isLoadingBalance) {
            // todo: maybe we should use skeleton for availableAmount too?
            // SwapWidget currently doesn't support skeleton for availableAmount
            mapTokenAmountText(BigDecimal.ZERO, currencyName)
        } else {
            // the "balance" is inconsistent with the "availableAmount" but it's clearly describes what it is,
            // and of course the "balance" is shorter than the "availableAmount"
            balance?.let { mapTokenAmountText(it, currencyName) }
        }
    }

    private fun mapAmount(isLoadingAmount: Boolean, amount: BigDecimal): TextViewCellModel {
        return if (isLoadingAmount) {
            mapTokenAmountSkeleton()
        } else {
            mapTokenAmountText(amount)
        }
    }

    private fun String.toRawTextViewCellModel(): TextViewCellModel {
        return TextViewCellModel.Raw(
            text = TextContainer(this)
        )
    }

    private fun mapWidgetTitle(titleRes: Int): TextViewCellModel.Raw {
        return TextViewCellModel.Raw(
            text = TextContainer(titleRes),
        )
    }

    private fun mapTokenAmountSkeleton(): TextViewCellModel = textCellSkeleton(
        height = 20.toPx(),
        width = 84.toPx(),
        radius = 6f.toPx(),
    )

    private fun mapTokenAmountText(amount: BigDecimal, currencyName: String? = null): TextViewCellModel {
        val amountText = amount.formatToken(STRIGA_FIAT_DECIMALS)
        return TextViewCellModel.Raw(
            TextContainer(
                if (currencyName != null) {
                    "$amountText $currencyName"
                } else {
                    amountText
                }

            )
        )
    }
}