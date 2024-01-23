package org.p2p.wallet.jupiter.ui.main.mapper

import java.math.BigDecimal
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.asUsdSwap
import org.p2p.core.utils.formatToken
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.utils.cutMiddle

class SwapWidgetTokenFormatter(
    private val jupiterSwapTokensRepository: JupiterSwapTokensRepository
) {
    fun formatFiatAmount(fiatAmount: BigDecimal): TextViewCellModel.Raw {
        val usd = fiatAmount.asUsdSwap()
        return TextViewCellModel.Raw(TextContainer(usd))
    }

    fun formatUserTokenAmount(
        token: SwapTokenModel,
        enteredTokenAmount: BigDecimal?
    ): TextViewCellModel.Raw {
        val decimals = token.decimals
        val amountText = enteredTokenAmount?.formatToken(
            decimals = decimals,
            keepInitialDecimals = true
        ).orEmpty()
        return TextViewCellModel.Raw(TextContainer(amountText))
    }

    suspend fun formatTokenName(token: SwapTokenModel): TextViewCellModel.Raw {
        val isStrictToken = jupiterSwapTokensRepository.findTokenByMint(token.mintAddress)?.isStrictToken ?: true
        val formattedTokenSymbol = buildString {
            if (token.tokenSymbol.length > 6) {
                append(token.tokenSymbol.cutMiddle(3))
            } else {
                append(token.tokenSymbol)
            }
            if (!isStrictToken) {
                append("️⚠")
            }
        }

        return TextViewCellModel.Raw(TextContainer(formattedTokenSymbol))
    }

    fun formatTokenBalance(token: SwapTokenModel): TextViewCellModel.Raw {
        val formattedTokenBalance = when (token) {
            is SwapTokenModel.JupiterToken -> {
                TextContainer(R.string.swap_main_balance_amount, "0")
            }
            is SwapTokenModel.UserToken -> {
                TextContainer(R.string.swap_main_balance_amount, formatUserTokenAmount(token, includeSymbol = false))
            }
        }
        return TextViewCellModel.Raw(formattedTokenBalance)
    }

    fun formatAvailableAmount(token: SwapTokenModel): TextViewCellModel.Raw? =
        when (token) {
            is SwapTokenModel.JupiterToken -> null
            is SwapTokenModel.UserToken -> TextViewCellModel.Raw(TextContainer(formatUserTokenAmount(token)))
        }

    private fun formatUserTokenAmount(token: SwapTokenModel.UserToken, includeSymbol: Boolean = true): String =
        token.details.getFormattedTotal(includeSymbol = includeSymbol)
}
