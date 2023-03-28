package org.p2p.wallet.home.model

import android.content.res.Resources
import java.math.BigDecimal
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.formatFiat
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.utils.toPx

class HomeMapper(
    private val resources: Resources
) {

    fun mapBalance(balance: BigDecimal): TextViewCellModel {
        val result = resources.getString(R.string.home_usd_format, balance.formatFiat())
        return TextViewCellModel.Raw(TextContainer(result))
    }

    fun mapRateSkeleton(): TextViewCellModel =
        TextViewCellModel.Skeleton(
            SkeletonCellModel(
                height = 16.toPx(),
                width = 160.toPx(),
                radius = 4f.toPx()
            )
        )
}
