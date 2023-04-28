package org.p2p.wallet.jupiter.ui.main

import io.mockk.every
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import org.p2p.core.utils.Constants
import org.p2p.core.utils.toLamports
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.jupiter.repository.model.JupiterSwapFees
import org.p2p.wallet.jupiter.repository.model.JupiterSwapMarketInformation
import org.p2p.wallet.jupiter.repository.model.JupiterSwapMode
import org.p2p.wallet.jupiter.repository.model.JupiterSwapPair
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.statemanager.price_impact.SwapPriceImpactView
import org.p2p.wallet.jupiter.ui.main.JupiterSwapTestHelpers.toReadableString
import org.p2p.wallet.jupiter.ui.main.widget.SwapWidgetModel
import org.p2p.wallet.transaction.ui.SwapTransactionBottomSheetData
import org.p2p.wallet.utils.Base58String

object JupiterSwapTestHelpers {
    val JUPITER_SOL_TOKEN = JupiterSwapToken(
        tokenMint = Base58String("So11111111111111111111111111111111111111112"),
        chainId = 101,
        decimals = 9,
        coingeckoId = "wrapped-solana",
        logoUri = "https://raw.githubusercontent.com/p2p-org/solana-token-list/main/assets/mainnet/So11111111111111111111111111111111111111112/logo.png",
        tokenName = "Wrapped SOL",
        tokenSymbol = "SOL",
        tags = listOf("solana", "sol", "wrapped-sol", "wrapped-solana")
    )

    val JUPITER_USDC_TOKEN = JupiterSwapToken(
        tokenMint = Base58String("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"),
        chainId = 101,
        decimals = 6,
        coingeckoId = "usd-coin",
        logoUri = "https://raw.githubusercontent.com/p2p-org/solana-token-list/main/assets/mainnet/EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v/logo.png",
        tokenName = "USD Coin",
        tokenSymbol = "USDC",
        tags = listOf("solana", "sol", "usdc", "usd-coin")
    )
    val DEFAULT_SWAPPABLE_TOKENS = listOf(
        JUPITER_SOL_TOKEN.tokenMint,
        JUPITER_USDC_TOKEN.tokenMint,
    )

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
    fun attachLogToViewCalls(view: JupiterSwapContract.View) {
        every { view.setFirstTokenWidgetState(any()) } answers {
            Timber.i("setFirstTokenWidgetState: ${arg<SwapWidgetModel>(0).toReadableString()}")
        }
        every { view.setSecondTokenWidgetState(any()) } answers {
            Timber.i("setSecondTokenWidgetState: ${arg<SwapWidgetModel>(0).toReadableString()}")
        }
        every { view.setRatioState(any()) } answers {
            Timber.i("setRatioState: ${arg<TextViewCellModel?>(0).toReadableString()}")
        }
        every { view.setButtonState(any()) } answers {
            Timber.i("setButtonState: ${arg<SwapButtonState>(0).toReadableString()}")
        }
        every { view.setAmountFiat(any()) } answers {
            Timber.i("setAmountFiat: ${arg<String>(0)}")
        }
        every { view.setAmountFiat(any()) } answers {
            Timber.i("setAmountFiat 2: ${arg<String>(0)}")
        }
        every { view.showSolErrorToast() } answers {
            Timber.i("showSolErrorToast")
        }
        every { view.closeScreen() } answers {
            Timber.i("closeScreen")
        }
        every { view.openChangeTokenAScreen() } answers {
            Timber.i("openChangeTokenAScreen")
        }
        every { view.openChangeTokenBScreen() } answers {
            Timber.i("openChangeTokenBScreen")
        }
        every { view.openChangeTokenBScreen() } answers {
            Timber.i("openChangeTokenBScreen")
        }
        every { view.showPriceImpact(any()) } answers {
            Timber.i("showPriceImpact: ${arg<SwapPriceImpactView>(0)}")
        }
        every { view.scrollToPriceImpact() } answers {
            Timber.i("scrollToPriceImpact")
        }
        every { view.showProgressDialog(any(), any()) } answers {
            Timber.i("showProgressDialog: ${arg<String>(0)} ${arg<SwapTransactionBottomSheetData>(1)}")
        }
        every { view.showDefaultSlider() } answers {
            Timber.i("showDefaultSlider")
        }
        every { view.showFullScreenError() } answers {
            Timber.i("showFullScreenError")
        }
        every { view.hideFullScreenError() } answers {
            Timber.i("hideFullScreenError")
        }
        every { view.showKeyboard() } answers {
            Timber.i("showKeyboard")
        }
    }

    class TestSwapRouteData {
        var inputMint: Base58String
        var outputMint: Base58String
        var amountIn: BigDecimal
        var amountOut: BigDecimal
        var priceImpact: BigDecimal = BigDecimal("0.0000001")
        var decimals: Int = 9

        constructor(
            inputMint: Base58String,
            outputMint: Base58String,
            amountIn: BigDecimal,
            amountOut: BigDecimal,
            priceImpact: BigDecimal = BigDecimal("0.0000001")
        ) {
            this.inputMint = inputMint
            this.outputMint = outputMint
            this.amountIn = amountIn
            this.amountOut = amountOut
            this.priceImpact = priceImpact
        }

        constructor(
            inputMint: Base58String,
            outputMint: Base58String,
            amountIn: BigDecimal,
            ratio: BigDecimal
        ) {
            this.inputMint = inputMint
            this.outputMint = outputMint
            this.amountIn = amountIn
            this.amountOut = amountIn * ratio
        }

        constructor(
            swapPair: JupiterSwapPair,
            userPublicKey: Base58String,
            ratio: BigDecimal = SOL_TO_USD_RATE,
            priceImpact: BigDecimal = BigDecimal("0.0000001")
        ) {
            inputMint = swapPair.inputMint
            outputMint = swapPair.outputMint
            amountIn = swapPair.amountInLamports.toBigDecimal(9)
            amountOut = amountIn * ratio
            this.priceImpact = priceImpact
        }
    }

    fun createSwapRoute(data: TestSwapRouteData): JupiterSwapRoute {
        val amountIn = data.amountIn.toLamports(data.decimals)
        val amountOut = data.amountOut.toLamports(data.decimals)

        return JupiterSwapRoute(
            amountInLamports = amountIn,
            inAmountInLamports = amountIn,
            outAmountInLamports = amountIn,
            priceImpactPct = data.priceImpact,
            marketInfos = listOf(
                JupiterSwapMarketInformation(
                    inputMint = data.inputMint,
                    outputMint = data.outputMint,
                    notEnoughLiquidity = false,
                    inAmountInLamports = amountIn,
                    outAmountInLamports = amountOut,
                    priceImpactPct = data.priceImpact,
                    id = "86eq4kdBkUCHGdCC2SfcqGHRCBGhp2M89aCmuvvxaXsm",
                    label = "Lifinity V2",
                    minInAmountInLamports = null,
                    minOutAmountInLamports = null,

                    liquidityFee = JupiterSwapMarketInformation.LpFee(
                        amountInLamports = BigInteger("400000"),
                        mint = JUPITER_SOL_TOKEN.tokenMint,
                        percent = BigDecimal("0.00040")
                    ),
                    platformFee = JupiterSwapMarketInformation.PlatformFee(
                        amountInLamports = BigInteger("0"),
                        mint = data.outputMint,
                        percent = BigDecimal("0.0")
                    )
                )
            ),
            slippageBps = 50,
            otherAmountThreshold = "22744918",
            swapMode = JupiterSwapMode.EXACT_IN,
            fees = JupiterSwapFees(
                signatureFee = BigInteger("0"),
                openOrdersDeposits = listOf(),
                ataDeposits = listOf(),
                totalFeeAndDepositsInSol = BigInteger("0"),
                minimumSolForTransaction = BigInteger("0")
            ),
            keyAppFee = "0",
            keyAppRefundableFee = "0",
            keyAppHash = "86cdfab8becfda70a17bac0c2b0704a3afe5458812a60af2e93154d11dc34abe"
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

    val SOL_TO_USD_RATE = BigDecimal("20.74")
    val USD_TO_SOL_RATE = BigDecimal.ONE.setScale(18, RoundingMode.UP).divide(SOL_TO_USD_RATE, RoundingMode.UP).setScale(18)

    fun createSOLToken(
        amount: BigDecimal = BigDecimal("100.3456"),
        rate: BigDecimal = SOL_TO_USD_RATE
    ): Token.Active =
        Token.createSOL(
            "some public key",
            TokenData(
                JUPITER_SOL_TOKEN.tokenMint.base58Value,
                "Solana",
                JUPITER_SOL_TOKEN.tokenSymbol,
                iconUrl = JUPITER_SOL_TOKEN.logoUri,
                decimals = JUPITER_SOL_TOKEN.decimals,
                isWrapped = false,
                serumV3Usdc = null,
                serumV3Usdt = null,
                coingeckoId = JUPITER_SOL_TOKEN.coingeckoId,
            ),
            amount.toLamports(JUPITER_SOL_TOKEN.decimals).toLong(),
            rate
        )

    fun createUSDCToken(amount: BigDecimal = BigDecimal("10.28")): Token.Active =
        Token.createSOL(
            "some public key",
            TokenData(
                JUPITER_USDC_TOKEN.tokenMint.base58Value,
                JUPITER_USDC_TOKEN.tokenName,
                JUPITER_USDC_TOKEN.tokenSymbol,
                iconUrl = JUPITER_USDC_TOKEN.logoUri,
                decimals = JUPITER_USDC_TOKEN.decimals,
                isWrapped = false,
                serumV3Usdc = null,
                serumV3Usdt = null,
                coingeckoId = JUPITER_USDC_TOKEN.coingeckoId
            ),
            amount.toLamports(JUPITER_USDC_TOKEN.decimals).toLong(),
            BigDecimal("1")
        )

    fun Token.Active.toTokenData(): TokenData {
        return TokenData(
            mintAddress = mintAddress,
            name = tokenName,
            symbol = tokenSymbol,
            iconUrl = iconUrl,
            decimals = decimals,
            isWrapped = isWrapped,
            serumV3Usdc = serumV3Usdc,
            serumV3Usdt = serumV3Usdt,
            coingeckoId = coingeckoId
        )
    }
}
