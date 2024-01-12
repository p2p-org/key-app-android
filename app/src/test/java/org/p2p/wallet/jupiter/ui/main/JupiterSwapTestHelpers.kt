package org.p2p.wallet.jupiter.ui.main

import com.google.gson.JsonObject
import io.mockk.every
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import org.p2p.core.common.TextContainer
import org.p2p.core.crypto.Base58String
import org.p2p.core.token.Token
import org.p2p.core.token.TokenExtensions
import org.p2p.core.token.TokenMetadata
import org.p2p.core.token.TokenMetadataExtension
import org.p2p.core.utils.Constants
import org.p2p.core.utils.toLamports
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoutePlanV6
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRouteV6
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.statemanager.price_impact.SwapPriceImpactView
import org.p2p.wallet.jupiter.ui.main.widget.SwapWidgetModel
import org.p2p.wallet.transaction.ui.SwapTransactionBottomSheetData

object JupiterSwapTestHelpers {
    val JUPITER_SOL_TOKEN = JupiterSwapToken(
        tokenMint = Base58String("So11111111111111111111111111111111111111112"),
        chainId = 101,
        decimals = 9,
        coingeckoId = "wrapped-solana",
        logoUri = "https://raw.githubusercontent.com/p2p-org/solana-token-list/main/assets/mainnet/So11111111111111111111111111111111111111112/logo.png",
        tokenName = "Wrapped SOL",
        tokenSymbol = "SOL",
        tags = listOf("solana", "sol", "wrapped-sol", "wrapped-solana"),
        tokenExtensions = TokenExtensions.NONE
    )

    val JUPITER_USDC_TOKEN = JupiterSwapToken(
        tokenMint = Base58String("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"),
        chainId = 101,
        decimals = 6,
        coingeckoId = "usd-coin",
        logoUri = "https://raw.githubusercontent.com/p2p-org/solana-token-list/main/assets/mainnet/EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v/logo.png",
        tokenName = "USD Coin",
        tokenSymbol = "USDC",
        tags = listOf("solana", "sol", "usdc", "usd-coin"),
        tokenExtensions = TokenExtensions.NONE
    )
    val DEFAULT_SWAPPABLE_TOKENS = listOf(
        JUPITER_SOL_TOKEN.tokenMint,
        JUPITER_USDC_TOKEN.tokenMint,
    )

    val SOL_TO_USD_RATE = BigDecimal("20.74")
    val USD_TO_SOL_RATE = BigDecimal.ONE.setScale(18, RoundingMode.UP).divide(SOL_TO_USD_RATE, RoundingMode.UP).setScale(18)

    fun SwapButtonState.toReadableString(): String {
        return when (this) {
            is SwapButtonState.Hide -> "SwapButtonState.Hide"
            is SwapButtonState.Disabled -> "SwapButtonState.Disabled(${text.toReadableString()})"
            is SwapButtonState.ReadyToSwap -> "SwapButtonState.ReadyToSwap(${text.toReadableString()})"
        }
    }

    fun TextContainer.toReadableString(): String {
        return when (this) {
            is TextContainer.Raw -> text.toString()
            is TextContainer.Res -> "res=$textRes"
            is TextContainer.ResParams -> "res=$textRes, params=$args"
        }
    }

    fun TextViewCellModel?.toReadableString(): String {
        return when (this) {
            is TextViewCellModel.Skeleton -> "Skeleton"
            is TextViewCellModel.Raw -> text.toReadableString()
            else -> "null"
        }
    }

    fun SwapWidgetModel.toReadableString(): String {
        return when (this) {
            is SwapWidgetModel.Loading -> "SwapWidgetModel.Loading(${widgetTitle.text.toReadableString()})"
            is SwapWidgetModel.Content -> {
                """

                SwapWidgetModel.Content(
                  widgetTitle=(${widgetTitle.toReadableString()})
                  balance=(${balance.toReadableString()})
                  amount=(${amount.toReadableString()})
                  fiatAmount=(${balance.toReadableString()})
                  amountMaxDecimals=$amountMaxDecimals
                  availableAmount=(${availableAmount.toReadableString()})
                  currencyName=(${currencyName.toReadableString()})
                )
                """.trimIndent()
            }
        }
    }

    /**
     * For debug purposes only
     * Note: `every` overwrites previous `every` call, so it should be used only to display which methods are calling
     */
    fun JupiterSwapContract.View.attachCallsLog() {
        val logger = Timber.tag("JupiterSwapContract.View")
        every { setFirstTokenWidgetState(any()) } answers {
            logger.i("setFirstTokenWidgetState: ${arg<SwapWidgetModel>(0).toReadableString()}")
        }
        every { setSecondTokenWidgetState(any()) } answers {
            logger.i("setSecondTokenWidgetState: ${arg<SwapWidgetModel>(0).toReadableString()}")
        }
        every { setRatioState(any()) } answers {
            logger.i("setRatioState: ${arg<TextViewCellModel?>(0).toReadableString()}")
        }
        every { setButtonState(any()) } answers {
            logger.i("setButtonState: ${arg<SwapButtonState>(0).toReadableString()}")
        }
        every { setAmountFiat(any()) } answers {
            logger.i("setAmountFiat: ${arg<String>(0)}")
        }
        every { setAmountFiat(any()) } answers {
            logger.i("setAmountFiat 2: ${arg<String>(0)}")
        }
        every { showSolErrorToast() } answers {
            logger.i("showSolErrorToast")
        }
        every { closeScreen() } answers {
            logger.i("closeScreen")
        }
        every { openChangeTokenAScreen() } answers {
            logger.i("openChangeTokenAScreen")
        }
        every { openChangeTokenBScreen() } answers {
            logger.i("openChangeTokenBScreen")
        }
        every { openChangeTokenBScreen() } answers {
            logger.i("openChangeTokenBScreen")
        }
        every { showPriceImpact(any()) } answers {
            logger.i("showPriceImpact: ${arg<SwapPriceImpactView>(0)}")
        }
        every { scrollToPriceImpact() } answers {
            logger.i("scrollToPriceImpact")
        }
        every { showProgressDialog(any(), any()) } answers {
            logger.i("showProgressDialog: ${arg<String>(0)} ${arg<SwapTransactionBottomSheetData>(1)}")
        }
        every { showDefaultSlider() } answers {
            logger.i("showDefaultSlider")
        }
        every { showFullScreenError() } answers {
            logger.i("showFullScreenError")
        }
        every { hideFullScreenError() } answers {
            logger.i("hideFullScreenError")
        }
        every { showKeyboard() } answers {
            logger.i("showKeyboard")
        }
    }

    fun BigInteger.minusPercent(value: Double): BigInteger {
        val source = toBigDecimal()
        val percent = BigDecimal.ONE - value.toBigDecimal()
        return (source * percent).toBigInteger()
    }

    fun createSwapRoute(data: TestSwapRouteData): JupiterSwapRouteV6 {
        val amountIn = data.amountIn.toLamports(data.inDecimals)
        val amountOut = data.amountOut.toLamports(data.outDecimals)

        return JupiterSwapRouteV6(
            inAmountInLamports = amountIn,
            outAmountInLamports = amountOut,
            routePlans = listOf(
                JupiterSwapRoutePlanV6(
                    inputMint = data.inputMint,
                    outputMint = data.outputMint,
                    outAmount = amountOut,
                    ammKey = "86eq4kdBkUCHGdCC2SfcqGHRCBGhp2M89aCmuvvxaXsm",
                    label = "Lifinity V2",
                    feeAmount = BigInteger("400000"),
                    feeMint = JUPITER_SOL_TOKEN.tokenMint,
                    percent = "100"
                )
            ),
            slippageBps = data.slippageBps,
            otherAmountThreshold = amountOut.minusPercent(data.slippage.doubleValue).toString(),
            swapMode = "EXACT_IN",
            ataFee = BigInteger.ZERO,
            priceImpactPercent = data.priceImpact,
            originalRoute = JsonObject()
        )
    }

    fun formatRateString(tokenASymbol: String, tokenBRate: String, tokenBSymbol: String): String {
        return "1 $tokenASymbol ≈ $tokenBRate $tokenBSymbol"
    }

    fun Token.isStableCoin(): Boolean = tokenSymbol == Constants.USDC_SYMBOL || tokenSymbol == Constants.USDT_SYMBOL

    fun getTokensInNonFiatToFiatOrder(tokenA: Token.Active, tokenB: Token.Active): Pair<Token.Active, Token.Active> {
        return if (!tokenA.isStableCoin() && tokenB.isStableCoin()) {
            tokenA to tokenB
        } else {
            tokenB to tokenA
        }
    }

    fun getRateFromUsd(usdRate: BigDecimal): BigDecimal {
        return BigDecimal.ONE.setScale(18, RoundingMode.UP).divide(usdRate, RoundingMode.UP).setScale(18)
    }

    fun createSOLToken(
        amount: BigDecimal = BigDecimal("100.3456"),
        rateToUsd: BigDecimal = SOL_TO_USD_RATE
    ): Token.Active =
        Token.createSOL(
            "some public key",
            TokenMetadata(
                JUPITER_SOL_TOKEN.tokenMint.base58Value,
                "Solana",
                JUPITER_SOL_TOKEN.tokenSymbol,
                iconUrl = JUPITER_SOL_TOKEN.logoUri,
                decimals = JUPITER_SOL_TOKEN.decimals,
                isWrapped = false,
                extensions = TokenMetadataExtension.NONE
            ),
            amount.toLamports(JUPITER_SOL_TOKEN.decimals).toLong(),
            rateToUsd
        )

    fun createUSDCToken(amount: BigDecimal = BigDecimal("10.28")): Token.Active =
        Token.createSOL(
            publicKey = "some public key",
            tokenMetadata = TokenMetadata(
                JUPITER_USDC_TOKEN.tokenMint.base58Value,
                JUPITER_USDC_TOKEN.tokenName,
                JUPITER_USDC_TOKEN.tokenSymbol,
                iconUrl = JUPITER_USDC_TOKEN.logoUri,
                decimals = JUPITER_USDC_TOKEN.decimals,
                isWrapped = false,
                extensions = TokenMetadataExtension.NONE
            ),
            amount = amount.toLamports(JUPITER_USDC_TOKEN.decimals).toLong(),
            solPrice = BigDecimal("1")
        )

    fun Token.Active.toTokenData(): TokenMetadata {
        return TokenMetadata(
            mintAddress = mintAddress,
            name = tokenName,
            symbol = tokenSymbol,
            iconUrl = iconUrl,
            decimals = decimals,
            isWrapped = isWrapped,
            extensions = TokenMetadataExtension.NONE
        )
    }
}
