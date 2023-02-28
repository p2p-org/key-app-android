package org.p2p.wallet.swap.ui.jupiter.main.mapper

import java.math.BigDecimal
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.emptyString
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.formatToken
import org.p2p.uikit.utils.skeleton.textCellSkeleton
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.ui.jupiter.main.SwapRateLoaderState
import org.p2p.wallet.swap.ui.jupiter.main.SwapTokenType
import org.p2p.wallet.swap.ui.jupiter.main.widget.SwapWidgetModel

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
        widgetModel: SwapWidgetModel.Content,
        tokenAmount: BigDecimal
    ): SwapWidgetModel.Content {
        return when (state) {
            SwapRateLoaderState.Empty,
            SwapRateLoaderState.Error,
            is SwapRateLoaderState.NoRateAvailable -> widgetModel.copy(fiatAmount = null)
            is SwapRateLoaderState.Loaded -> widgetModel.copy(
                fiatAmount = fiatAmount(
                    fiatAmount = tokenAmount.multiply(state.rate)
                ),
                amount = tokenAmount(state.token, tokenAmount)
            )
            SwapRateLoaderState.Loading -> widgetModel.copy(
                fiatAmount = textCellSkeleton(
                    height = 8.toPx(),
                    width = 84.toPx(),
                    radius = 2f.toPx(),
                )
            )
        }
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
        val usd = fiatAmount.formatFiat()
        return TextViewCellModel.Raw(TextContainer(R.string.swap_main_fiat_value, usd))
    }

    private fun tokenAmount(token: SwapTokenModel, tokenAmount: BigDecimal?): TextViewCellModel.Raw {
        val decimals = token.decimals
        val amountText = tokenAmount?.formatToken(decimals) ?: emptyString()
        return TextViewCellModel.Raw(TextContainer(amountText))
    }

    private fun tokenName(token: SwapTokenModel): TextViewCellModel.Raw =
        TextViewCellModel.Raw(TextContainer(token.tokenSymbol))

    private fun balance(token: SwapTokenModel): TextViewCellModel.Raw? =
        when (token) {
            is SwapTokenModel.JupiterToken -> null
            is SwapTokenModel.UserToken -> balanceText(token)
        }

    private fun balanceText(token: SwapTokenModel.UserToken): TextViewCellModel.Raw =
        TextViewCellModel.Raw(
            TextContainer(R.string.swap_main_balance_amount, tokenAmount(token))
        )

    private fun availableAmount(token: SwapTokenModel): TextViewCellModel.Raw? =
        when (token) {
            is SwapTokenModel.JupiterToken -> null
            is SwapTokenModel.UserToken -> availableAmountText(token)
        }

    private fun availableAmountText(token: SwapTokenModel.UserToken): TextViewCellModel.Raw =
        TextViewCellModel.Raw(
            TextContainer(tokenAmount(token))
        )

    private fun tokenAmount(token: SwapTokenModel.UserToken): String =
        token.details.getFormattedTotal()

    private fun swapWidgetFromTitle(): TextViewCellModel.Raw =
        TextViewCellModel.Raw(TextContainer(R.string.swap_main_you_pay))

    private fun swapWidgetToTitle(): TextViewCellModel.Raw =
        TextViewCellModel.Raw(TextContainer(R.string.swap_main_you_receive))
}
