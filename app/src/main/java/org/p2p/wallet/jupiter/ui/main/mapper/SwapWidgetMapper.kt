package org.p2p.wallet.jupiter.ui.main.mapper

import java.math.BigDecimal
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.asUsdSwap
import org.p2p.core.utils.emptyString
import org.p2p.core.utils.formatToken
import org.p2p.uikit.utils.skeleton.textCellSkeleton
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.statemanager.price_impact.SwapPriceImpactView
import org.p2p.wallet.jupiter.ui.main.SwapRateLoaderState
import org.p2p.wallet.jupiter.ui.main.SwapTokenType
import org.p2p.wallet.jupiter.ui.main.widget.SwapWidgetModel

class SwapWidgetMapper {

    fun mapWidgetLoading(tokenType: SwapTokenType): SwapWidgetModel {
        return when (tokenType) {
            SwapTokenType.TOKEN_A -> SwapWidgetModel.Loading(
                isStatic = false,
                widgetTitle = TextViewCellModel.Raw(text = TextContainer(R.string.swap_main_you_pay)),
            )
            SwapTokenType.TOKEN_B -> SwapWidgetModel.Loading(
                isStatic = true,
                widgetTitle = TextViewCellModel.Raw(text = TextContainer(R.string.swap_main_you_receive)),
            )
        }
    }

    fun mapFiatAmount(
        state: SwapRateLoaderState,
        oldWidgetModel: SwapWidgetModel,
        tokenAmount: BigDecimal
    ): SwapWidgetModel {
        val widgetModel = oldWidgetModel as? SwapWidgetModel.Content ?: return oldWidgetModel
        val oldFiatAmount = (oldWidgetModel as? SwapWidgetModel.Content)?.fiatAmount as? TextViewCellModel.Raw
        return when (state) {
            SwapRateLoaderState.Empty,
            SwapRateLoaderState.Error,
            is SwapRateLoaderState.NoRateAvailable -> widgetModel.copy(fiatAmount = null)
            is SwapRateLoaderState.Loaded -> {
                var newFiatAmount = fiatAmount(fiatAmount = tokenAmount.multiply(state.rate))
                oldFiatAmount?.let { newFiatAmount = newFiatAmount.copy(textColor = oldFiatAmount.textColor) }
                widgetModel.copy(
                    fiatAmount = newFiatAmount,
                )
            }
            SwapRateLoaderState.Loading -> widgetModel.copy(
                fiatAmount = textCellSkeleton(
                    height = 8.toPx(),
                    width = 84.toPx(),
                    radius = 2f.toPx(),
                )
            )
        }
    }

    fun copyAmount(
        oldWidgetModel: SwapWidgetModel,
        token: SwapTokenModel,
        tokenAmount: BigDecimal,
    ): SwapWidgetModel {
        val widgetModel = oldWidgetModel as? SwapWidgetModel.Content ?: return oldWidgetModel
        val oldAmount = widgetModel.amount as? TextViewCellModel.Raw ?: return oldWidgetModel
        return widgetModel.copy(
            amount = tokenAmount(token, tokenAmount)
                .copy(textColor = oldAmount.textColor)
        )
    }

    fun copyAmountWithSourceValue(
        oldWidgetModel: SwapWidgetModel,
        userInputTokenAmount: String,
    ): SwapWidgetModel {
        val widgetModel = oldWidgetModel as? SwapWidgetModel.Content ?: return oldWidgetModel
        val oldAmount = widgetModel.amount as? TextViewCellModel.Raw ?: return oldWidgetModel
        return widgetModel.copy(
            amount = TextViewCellModel.Raw(TextContainer(userInputTokenAmount))
                .copy(textColor = oldAmount.textColor)
        )
    }

    fun mapTokenAAndSaveOldFiatAmount(
        oldWidgetModel: SwapWidgetModel,
        token: SwapTokenModel,
        tokenAmount: BigDecimal? = null,
    ): SwapWidgetModel {
        var result = mapTokenA(token, tokenAmount)
        if (oldWidgetModel is SwapWidgetModel.Content && oldWidgetModel.fiatAmount != null) {
            result = result.copy(fiatAmount = oldWidgetModel.fiatAmount)
        }
        return result
    }

    fun mapTokenA(
        token: SwapTokenModel,
        tokenAmount: BigDecimal? = null,
    ): SwapWidgetModel.Content {
        return SwapWidgetModel.Content(
            isStatic = false,
            widgetTitle = swapWidgetFromTitle(),
            availableAmount = availableAmount(token),
            balance = balance(token),
            currencyName = tokenName(token),
            amount = tokenAmount(token, tokenAmount),
            fiatAmount = null,
            amountMaxDecimals = token.decimals,
        )
    }

    fun mapTokenBAndSaveOldFiatAmount(
        oldWidgetModel: SwapWidgetModel,
        token: SwapTokenModel,
        tokenAmount: BigDecimal? = null,
    ): SwapWidgetModel {
        var result = mapTokenB(token, tokenAmount)
        if (oldWidgetModel is SwapWidgetModel.Content && oldWidgetModel.fiatAmount != null) {
            result = result.copy(fiatAmount = oldWidgetModel.fiatAmount)
        }
        return result
    }

    fun mapTokenB(token: SwapTokenModel, tokenAmount: BigDecimal?): SwapWidgetModel.Content {
        return SwapWidgetModel.Content(
            isStatic = true,
            widgetTitle = swapWidgetToTitle(),
            availableAmount = null,
            balance = balance(token),
            currencyName = tokenName(token),
            amount = tokenAmount(token, tokenAmount),
            fiatAmount = null,
            amountMaxDecimals = token.decimals,
        )
    }

    fun mapTokenBLoading(
        token: SwapTokenModel,
    ): SwapWidgetModel.Content =
        SwapWidgetModel.Content(
            isStatic = true,
            widgetTitle = swapWidgetToTitle(),
            currencyName = tokenName(token),
            amount = textCellSkeleton(
                height = 20.toPx(),
                width = 84.toPx(),
                radius = 6f.toPx(),
            ),
            balance = balance(token),
            fiatAmount = null,
            availableAmount = null,
        )

    private fun fiatAmount(fiatAmount: BigDecimal): TextViewCellModel.Raw {
        val usd = fiatAmount.asUsdSwap()
        return TextViewCellModel.Raw(TextContainer(usd))
    }

    private fun tokenAmount(token: SwapTokenModel, tokenAmount: BigDecimal?): TextViewCellModel.Raw {
        val decimals = token.decimals
        val amountText = tokenAmount
            ?.formatToken(
                decimals = decimals,
                keepInitialDecimals = true
            )
            ?: emptyString()
        return TextViewCellModel.Raw(TextContainer(amountText))
    }

    private fun tokenName(token: SwapTokenModel): TextViewCellModel.Raw =
        TextViewCellModel.Raw(TextContainer(token.tokenSymbol))

    private fun balance(token: SwapTokenModel): TextViewCellModel.Raw =
        when (token) {
            is SwapTokenModel.JupiterToken -> TextViewCellModel.Raw(
                TextContainer(R.string.swap_main_balance_amount, "0")
            )
            is SwapTokenModel.UserToken -> TextViewCellModel.Raw(
                TextContainer(R.string.swap_main_balance_amount, tokenAmount(token, includeSymbol = false))
            )
        }

    private fun availableAmount(token: SwapTokenModel): TextViewCellModel.Raw? =
        when (token) {
            is SwapTokenModel.JupiterToken -> null
            is SwapTokenModel.UserToken -> availableAmountText(token)
        }

    private fun availableAmountText(token: SwapTokenModel.UserToken): TextViewCellModel.Raw =
        TextViewCellModel.Raw(
            TextContainer(tokenAmount(token))
        )

    private fun tokenAmount(token: SwapTokenModel.UserToken, includeSymbol: Boolean = true): String =
        token.details.getFormattedTotal(includeSymbol = includeSymbol)

    private fun swapWidgetFromTitle(): TextViewCellModel.Raw =
        TextViewCellModel.Raw(TextContainer(R.string.swap_main_you_pay))

    private fun swapWidgetToTitle(): TextViewCellModel.Raw =
        TextViewCellModel.Raw(TextContainer(R.string.swap_main_you_receive))

    fun mapErrorTokenAAmount(
        tokenA: SwapTokenModel?,
        oldWidgetAState: SwapWidgetModel,
        notValidAmount: BigDecimal
    ): SwapWidgetModel {
        if (tokenA == null) return oldWidgetAState
        val result = oldWidgetAState as? SwapWidgetModel.Content ?: return oldWidgetAState
        return result.copy(
            amount = tokenAmount(tokenA, notValidAmount).copy(textColor = R.color.text_rose),
        )
    }

    fun mapPriceImpact(oldWidgetModel: SwapWidgetModel, priceImpact: SwapPriceImpactView): SwapWidgetModel {
        val fiatAmount = (oldWidgetModel as? SwapWidgetModel.Content)
            ?.fiatAmount as? TextViewCellModel.Raw
            ?: return oldWidgetModel
        return when (priceImpact) {
            SwapPriceImpactView.Hidden -> {
                oldWidgetModel
            }
            is SwapPriceImpactView.Yellow -> {
                oldWidgetModel.copy(fiatAmount = fiatAmount.copy(textColor = R.color.text_sun))
            }
            is SwapPriceImpactView.Red -> {
                oldWidgetModel.copy(fiatAmount = fiatAmount.copy(textColor = R.color.text_rose))
            }
        }
    }
}
