package org.p2p.wallet.pnl.ui

import android.view.Gravity
import org.p2p.core.common.TextContainer
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.pnl.models.PnlData
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

    fun mapBalancePnl(pnlData: PnlData?): TextViewCellModel {
        if (pnlData == null) return mapBalancePnlLoading()

        return TextViewCellModel.Raw(
            TextContainer(R.string.home_pnl_format, pnlData.total.percent)
        )
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

    fun mapTokenBalancePnl(pnlTokenData: PnlTokenData?): TextViewCellModel {
        if (pnlTokenData == null) {
            return mapBalancePnlLoading()
        }
        return TextViewCellModel.Raw(
            TextContainer(R.string.home_pnl_format, pnlTokenData.percent)
        )
    }
}
