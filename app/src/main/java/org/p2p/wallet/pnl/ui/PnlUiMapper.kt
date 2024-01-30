package org.p2p.wallet.pnl.ui

import android.content.res.Resources
import android.view.Gravity
import org.p2p.core.common.TextContainer
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.pnl.models.PnlData
import org.p2p.wallet.pnl.models.PnlTokenData
import org.p2p.wallet.utils.toPx

class PnlUiMapper(
    private val resources: Resources,
) {

    fun getFormattedPnlForToken(pnlTokenData: PnlTokenData?): TextViewCellModel? {
        if (pnlTokenData == null) return null
        val result = resources.getString(R.string.token_pnl_format, pnlTokenData.percent)
        return TextViewCellModel.Raw(
            text = org.p2p.core.common.TextContainer(result),
            // textColor = if(pnlTokenData.isNegative) R.color.text_mountain else R.color.text_mountain
            textColor = R.color.text_mountain
        )
    }

    fun mapBalancePnl(pnlData: PnlData?): TextViewCellModel {
        if (pnlData == null) return mapBalancePnlLoading()

        val result = resources.getString(R.string.home_pnl_format, pnlData.total.percent)
        return TextViewCellModel.Raw(TextContainer(result))
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
        val result = resources.getString(R.string.home_pnl_format, pnlTokenData.percent)
        return TextViewCellModel.Raw(TextContainer(result))
    }
}
