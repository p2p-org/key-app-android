package org.p2p.wallet.swap.ui.jupiter.main.mapper

import java.math.BigDecimal
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.formatToken
import org.p2p.uikit.utils.skeleton.textCellSkeleton
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel
import org.p2p.wallet.swap.ui.jupiter.main.widget.SwapWidgetModel

class SwapWidgetMapper {

    fun mapWidgetLoading(isTokenA: Boolean): SwapWidgetModel {
        return if (isTokenA) {
            SwapWidgetModel.Loading(
                isStatic = false,
                widgetTitle = TextViewCellModel.Raw(text = TextContainer(R.string.swap_main_you_pay)),
            )
        } else {
            SwapWidgetModel.Loading(
                isStatic = true,
                widgetTitle = TextViewCellModel.Raw(text = TextContainer(R.string.swap_main_you_receive)),
            )
        }
    }

    fun mapTokenA(
        token: SwapTokenModel,
        tokenAmount: BigDecimal? = null,
    ): SwapWidgetModel {
        return SwapWidgetModel.Content(
            isStatic = false,
            widgetTitle = swapWidgetFromTitle(),
            availableAmount = availableAmount(token),
            balance = balance(token),
            currencyName = tokenName(token),
            amount = tokenAmount(token, tokenAmount),
            fiatAmount = fiatAmount(token, tokenAmount),
            amountMaxDecimals = token.decimals,
        )
    }

    fun mapTokenB(token: SwapTokenModel, tokenAmount: BigDecimal?): SwapWidgetModel {
        var fiatAmount = fiatAmount(token, tokenAmount)
        if (true) {
            // todo price impact
            fiatAmount = fiatAmount?.copy(textColor = R.color.text_night)
        }
        return SwapWidgetModel.Content(
            isStatic = true,
            widgetTitle = swapWidgetToTitle(),
            availableAmount = null,
            balance = balance(token),
            currencyName = tokenName(token),
            amount = tokenAmount(token, tokenAmount),
            fiatAmount = fiatAmount,
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

    private fun fiatAmount(token: SwapTokenModel, tokenAmount: BigDecimal?): TextViewCellModel.Raw? {
        if (tokenAmount == null) return null
        val ratio = when (token) {
            is SwapTokenModel.JupiterToken -> BigDecimal.ZERO
            is SwapTokenModel.UserToken -> token.details.totalInUsd
        } ?: return null

        val usd = tokenAmount.multiply(ratio).formatFiat()
        return TextViewCellModel.Raw(TextContainer(R.string.swap_main_fiat_value, usd))
    }

    private fun tokenAmount(token: SwapTokenModel, tokenAmount: BigDecimal?): TextViewCellModel.Raw {
        val decimals = token.decimals
        val amountText = tokenAmount?.formatToken(decimals) ?: "0"
        return TextViewCellModel.Raw(TextContainer(amountText))
    }

    private fun tokenName(token: SwapTokenModel): TextViewCellModel.Raw =
        TextViewCellModel.Raw(TextContainer(token.tokenName))

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
            TextContainer(R.string.swap_main_available_amount, tokenAmount(token))
        )

    private fun tokenAmount(token: SwapTokenModel.UserToken): String =
        token.details.getFormattedTotal()

    private fun swapWidgetFromTitle(): TextViewCellModel.Raw =
        TextViewCellModel.Raw(TextContainer(R.string.swap_main_you_pay))

    private fun swapWidgetToTitle(): TextViewCellModel.Raw =
        TextViewCellModel.Raw(TextContainer(R.string.swap_main_you_receive))
}
