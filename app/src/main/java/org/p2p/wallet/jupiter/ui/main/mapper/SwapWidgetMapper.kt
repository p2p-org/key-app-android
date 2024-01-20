package org.p2p.wallet.jupiter.ui.main.mapper

import java.math.BigDecimal
import org.p2p.core.common.TextContainer
import org.p2p.uikit.utils.skeleton.textCellSkeleton
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.statemanager.price_impact.SwapPriceImpactView
import org.p2p.wallet.jupiter.ui.main.SwapRateLoaderState
import org.p2p.wallet.jupiter.ui.main.SwapTokenType
import org.p2p.wallet.jupiter.ui.main.widget.SwapWidgetModel

class SwapWidgetMapper(
    private val tokenFormatter: SwapWidgetTokenFormatter
) {

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
            is SwapRateLoaderState.NoRateAvailable -> {
                widgetModel.copy(fiatAmount = null)
            }
            is SwapRateLoaderState.Loaded -> {
                var newFiatAmount = tokenFormatter.formatFiatAmount(fiatAmount = tokenAmount.multiply(state.rate))
                oldFiatAmount?.also { newFiatAmount = newFiatAmount.copy(textColor = oldFiatAmount.textColor) }
                widgetModel.copy(fiatAmount = newFiatAmount)
            }
            SwapRateLoaderState.Loading -> {
                widgetModel.copy(
                    fiatAmount = textCellSkeleton(
                        height = 8.toPx(),
                        width = 84.toPx(),
                        radius = 2f.toPx(),
                    )
                )
            }
        }
    }

    fun copyAmount(
        oldWidgetModel: SwapWidgetModel,
        token: SwapTokenModel,
        enteredTokenAmount: BigDecimal,
    ): SwapWidgetModel {
        val widgetModel = oldWidgetModel as? SwapWidgetModel.Content ?: return oldWidgetModel
        val oldAmount = widgetModel.amount as? TextViewCellModel.Raw ?: return oldWidgetModel
        return widgetModel.copy(
            amount = tokenFormatter.formatUserTokenAmount(token, enteredTokenAmount)
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

    suspend fun mapTokenAAndSaveOldFiatAmount(
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

    suspend fun mapTokenA(
        token: SwapTokenModel,
        enteredTokenAmount: BigDecimal? = null,
    ): SwapWidgetModel.Content {
        return SwapWidgetModel.Content(
            isStatic = false,
            widgetTitle = swapWidgetFromTitle(),
            availableAmount = tokenFormatter.formatAvailableAmount(token),
            balance = tokenFormatter.formatTokenBalance(token),
            currencyName = tokenFormatter.formatTokenName(token),
            tokenUrl = token.iconUrl,
            amount = tokenFormatter.formatUserTokenAmount(token, enteredTokenAmount),
            fiatAmount = null,
            amountMaxDecimals = token.decimals,
        )
    }

    suspend fun mapTokenBAndSaveOldFiatAmount(
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

    suspend fun mapTokenB(token: SwapTokenModel, enteredTokenAmount: BigDecimal?): SwapWidgetModel.Content {
        return SwapWidgetModel.Content(
            isStatic = true,
            widgetTitle = swapWidgetToTitle(),
            availableAmount = null,
            tokenUrl = token.iconUrl,
            balance = tokenFormatter.formatTokenBalance(token),
            currencyName = tokenFormatter.formatTokenName(token),
            amount = tokenFormatter.formatUserTokenAmount(token, enteredTokenAmount),
            fiatAmount = null,
            amountMaxDecimals = token.decimals,
        )
    }

    suspend fun mapTokenBLoading(token: SwapTokenModel): SwapWidgetModel.Content {
        return SwapWidgetModel.Content(
            isStatic = true,
            widgetTitle = swapWidgetToTitle(),
            tokenUrl = token.iconUrl,
            currencyName = tokenFormatter.formatTokenName(token),
            amount = textCellSkeleton(
                height = 20.toPx(),
                width = 84.toPx(),
                radius = 6f.toPx(),
            ),
            balance = tokenFormatter.formatTokenBalance(token),
            fiatAmount = null,
            availableAmount = null,
        )
    }

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
            amount = tokenFormatter.formatUserTokenAmount(tokenA, notValidAmount).copy(textColor = R.color.text_rose),
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
