package org.p2p.wallet.pnl.ui

import android.view.Gravity
import org.p2p.core.common.TextContainer
import org.p2p.core.crypto.Base58String
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.pnl.interactor.PnlDataState
import org.p2p.wallet.pnl.models.PnlTokenData
import org.p2p.wallet.utils.toPx

class PnlUiMapper {

    fun getFormattedPnlForToken(pnlTokenData: PnlTokenData?): TextViewCellModel? {
        pnlTokenData ?: return null

        return TextViewCellModel.Raw(
            text = TextContainer(R.string.token_pnl_format, pnlTokenData.percent),
            textColor = R.color.text_mountain
        )
    }

    fun mapBalancePnl(pnlDataState: PnlDataState): TextViewCellModel? {
        return when (pnlDataState) {
            is PnlDataState.Result -> {
                TextViewCellModel.Raw(
                    TextContainer(R.string.home_pnl_format, pnlDataState.data.total.percent)
                )
            }
            is PnlDataState.Loading -> {
                mapBalancePnlLoading()
            }
            is PnlDataState.Error -> {
                null
            }
        }
    }

    fun mapTokenBalancePnl(tokenMint: Base58String, pnlDataState: PnlDataState): TextViewCellModel? {
        return when (pnlDataState) {
            is PnlDataState.Result -> {
                pnlDataState.findForToken(tokenMint)?.let { pnlTokenData ->
                    TextViewCellModel.Raw(
                        TextContainer(R.string.home_pnl_format, pnlTokenData.percent)
                    )
                }
            }
            is PnlDataState.Loading -> {
                mapBalancePnlLoading()
            }
            is PnlDataState.Error -> {
                null
            }
        }
    }

    private fun mapBalancePnlLoading(): TextViewCellModel {
        return TextViewCellModel.Skeleton(
            SkeletonCellModel(
                height = 20.toPx(),
                width = 100.toPx(),
                radius = 8f.toPx(),
                gravity = Gravity.CENTER
            )
        )
    }
}
