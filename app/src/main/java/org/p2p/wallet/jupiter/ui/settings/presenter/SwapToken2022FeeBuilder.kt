package org.p2p.wallet.jupiter.ui.settings.presenter

import java.math.BigDecimal
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.asUsdSwap
import org.p2p.core.utils.formatFiat
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

    suspend fun buildToken2022Fees(tokenA: SwapTokenModel, tokenAAmount: BigDecimal): SwapSettingsFeeBox? {
        val extensions = getTokenExtensionsUseCase.execute(tokenA.mintAddress)
        val interestFee = extensions.interestBearingConfig?.currentRate?.toBigDecimal()
        // example: BERN
        val transferFeePercent = extensions.transferFeeConfig
            ?.getActualTransferFee(solanaRepository.getEpochInfo().epoch)
            ?.transferFeePercent

        val usdTokenAAmount = rateLoader.getLoadedRate(tokenA)?.let { it.rate * tokenAAmount }

        val transferFeeUsd = if (transferFeePercent != null && usdTokenAAmount != null) {
            transferFeePercent.divide(100.toBigDecimal()) * usdTokenAAmount
        } else {
            null
        }

        val interestFeeCell = interestFee?.let {
            MainCellModel(
                leftSideCellModel = LeftSideCellModel.IconWithText(
                    firstLineText = TextViewCellModel.Raw(
                        text = TextContainer(R.string.swap_settings_interest_fee_title),
                    ),
                    secondLineText = TextViewCellModel.Raw(
                        text = TextContainer("${it.formatFiat()}%")
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

        val transferFeeCell = transferFeePercent?.let {
            MainCellModel(
                leftSideCellModel = LeftSideCellModel.IconWithText(
                    firstLineText = TextViewCellModel.Raw(
                        text = TextContainer(R.string.swap_settings_transfer_fee_title),
                    ),
                    secondLineText = TextViewCellModel.Raw(
                        text = TextContainer("${it.formatFiat()}%")
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
        return (interestFeeCell ?: transferFeeCell)?.let {
            SwapSettingsFeeBox(
                cellModel = it,
                feeInUsd = transferFeeUsd
            )
        }
    }
}
