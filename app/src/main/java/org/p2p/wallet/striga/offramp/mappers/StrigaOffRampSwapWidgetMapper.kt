package org.p2p.wallet.striga.offramp.mappers

import androidx.annotation.StyleRes
import android.util.TypedValue
import java.math.BigDecimal
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.STRIGA_FIAT_DECIMALS
import org.p2p.core.utils.emptyString
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.isZero
import org.p2p.uikit.utils.skeleton.textCellSkeleton
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.TextViewSize
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
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
            currencyName = currencyName.toRawTextViewCellModel(
                textSizeSp = 28f,
                textAppearanceResId = R.style.UiKit_TextAppearance_Regular_Title1
            ),
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
            mapTokenAmountText(BigDecimal.ZERO.formatAmount(), currencyName)
        } else {
            // the "balance" is inconsistent with the "availableAmount" but it's clearly describes what it is,
            // and of course the "balance" is shorter than the "availableAmount"
            balance?.let { mapTokenAmountText(it.formatAmount(), currencyName) }
        }
    }

    private fun mapAmount(isLoadingAmount: Boolean, amount: BigDecimal): TextViewCellModel {
        return if (isLoadingAmount) {
            mapTokenAmountSkeleton()
        } else {
            // zero is shown in the EditTexts' hint
            mapTokenAmountText(if (amount.isZero()) emptyString() else amount.formatAmount())
        }
    }

    private fun String.toRawTextViewCellModel(
        textSizeSp: Float = 16f,
        @StyleRes textAppearanceResId: Int = R.style.UiKit_TextAppearance_Regular_Title1,
    ): TextViewCellModel {
        return TextViewCellModel.Raw(
            text = TextContainer(this),
            textAppearance = textAppearanceResId,
            textSize = TextViewSize(textSizeSp, TypedValue.COMPLEX_UNIT_SP),
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

    private fun mapTokenAmountText(amount: String, currencyName: String? = null): TextViewCellModel {
        return TextViewCellModel.Raw(
            text = TextContainer(
                if (currencyName != null) {
                    "$amount $currencyName"
                } else {
                    amount
                }

            ),
            textAppearance = R.style.UiKit_TextAppearance_Regular_Title1
        )
    }

    private fun BigDecimal.formatAmount(): String {
        return this.formatToken(STRIGA_FIAT_DECIMALS)
    }
}
