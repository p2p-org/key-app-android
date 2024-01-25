package org.p2p.wallet.jupiter.ui.settings.presenter

import java.math.BigDecimal
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.asUsdSwap
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.isNotZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.plusAssign
import org.p2p.solanaj.kits.AccountInfoTokenExtensionConfig.Companion.interestBearingConfig
import org.p2p.solanaj.kits.AccountInfoTokenExtensionConfig.Companion.transferFeeConfig
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.finance_block.MainCellStyle
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.ui.main.SwapTokenRateLoader
import org.p2p.wallet.send.interactor.usecase.GetTokenExtensionsUseCase

class SwapToken2022FeeBuilder(
    private val getTokenExtensionsUseCase: GetTokenExtensionsUseCase,
    private val rateLoader: SwapTokenRateLoader,
    private val solanaRepository: RpcSolanaRepository
) {

    suspend fun buildToken2022Fees(
        tokenA: SwapTokenModel,
        tokenAAmount: BigDecimal,
        tokenB: SwapTokenModel,
        tokenBAmount: BigDecimal?
    ): SwapSettingsFeeBox? {
        if (!tokenA.isToken2022 && !tokenB.isToken2022) return null

        val transferFeePercentsTitle = StringBuilder()
        val interestBearingPercentsTitle = StringBuilder()

        var transferFeeTotalUsd = BigDecimal.ZERO

        if (tokenA.isToken2022) {
            val (transferFeePercent, interestFeePercent) = getToken2022Fees(tokenA)
            if (transferFeePercent != null) {
                transferFeePercentsTitle += "${transferFeePercent.formatFiat()}% ${tokenA.tokenSymbol}"
                transferFeePercentsTitle += " " // space for the next token if there will be

                val transferFeeUsd = getTransferFeeUsdRate(tokenA, tokenAAmount, transferFeePercent)
                transferFeeTotalUsd += transferFeeUsd.orZero()
            }
            if (interestFeePercent != null) {
                interestBearingPercentsTitle += "${interestFeePercent.formatFiat()}% ${tokenA.tokenSymbol}"
                interestBearingPercentsTitle += " " // space for the next token if there will be
            }
        }

        if (tokenB.isToken2022) {
            val (transferFeePercent, interestFeePercent) = getToken2022Fees(tokenB)
            if (transferFeePercent != null) {
                transferFeePercentsTitle += "${transferFeePercent.formatFiat()}% ${tokenB.tokenSymbol}"

                val transferFeeUsd = getTransferFeeUsdRate(tokenB, tokenBAmount.orZero(), transferFeePercent)
                transferFeeTotalUsd += transferFeeUsd.orZero()
            }
            if (interestFeePercent != null) {
                interestBearingPercentsTitle += "${interestFeePercent.formatFiat()}% ${tokenB.tokenSymbol}"
            }
        }

        val interestFeeCell = interestBearingPercentsTitle.toString()
            .takeIf { it.isNotBlank() }
            ?.let {
                MainCellModel(
                    leftSideCellModel = LeftSideCellModel.IconWithText(
                        firstLineText = TextViewCellModel.Raw(
                            text = TextContainer(R.string.swap_settings_interest_fee_title),
                        ),
                        secondLineText = TextViewCellModel.Raw(
                            text = TextContainer(it)
                        ),
                    ),
                    rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                        text = null,
                        firstIcon = ImageViewCellModel(
                            icon = DrawableContainer(R.drawable.ic_info_outline),
                            iconTint = R.color.icons_mountain,
                        )
                    ),
                    payload = SwapSettingsPayload.TOKEN_2022_INTEREST,
                    styleType = MainCellStyle.BASE_CELL,
                )
            }

        val transferFeeCell = createTransferFeeCell(
            transferFeeTitle = transferFeePercentsTitle.toString(),
            transferFeeUsd = transferFeeTotalUsd.takeIf { it.isNotZero() }
        )
        return (transferFeeCell ?: interestFeeCell)?.let {
            SwapSettingsFeeBox(
                cellModel = it,
                feeInUsd = transferFeeTotalUsd
            )
        }
    }

    private suspend fun getToken2022Fees(token: SwapTokenModel): Pair<BigDecimal?, BigDecimal?> {
        val extensions = getTokenExtensionsUseCase.execute(token.mintAddress)
        val interestFee = extensions.interestBearingConfig?.currentRate?.toBigDecimal()
        // example: BERN
        val transferFeePercent = extensions.transferFeeConfig
            ?.getActualTransferFee(solanaRepository.getEpochInfo().epoch)
            ?.transferFeePercent

        return transferFeePercent to interestFee
    }

    private suspend fun getTransferFeeUsdRate(
        token: SwapTokenModel,
        tokenAmount: BigDecimal,
        transferFeePercent: BigDecimal
    ): BigDecimal? {
        val usdTokenAAmount = rateLoader.getLoadedRate(token)?.let { it.rate * tokenAmount }

        return usdTokenAAmount?.let {
            transferFeePercent.divide(100.toBigDecimal()) * it
        }
    }

    private fun createTransferFeeCell(
        transferFeeTitle: String,
        transferFeeUsd: BigDecimal?
    ): MainCellModel {
        return MainCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_settings_transfer_fee_title),
                ),
                secondLineText = TextViewCellModel.Raw(
                    text = TextContainer(transferFeeTitle)
                ),
            ),
            rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                text = transferFeeUsd?.let {
                    TextViewCellModel.Raw(text = TextContainer(it.asUsdSwap()))
                },
                firstIcon = ImageViewCellModel(
                    icon = DrawableContainer(R.drawable.ic_info_outline),
                    iconTint = R.color.icons_mountain,
                )
            ),
            payload = SwapSettingsPayload.TOKEN_2022_TRANSFER,
            styleType = MainCellStyle.BASE_CELL,
        )
    }
}
