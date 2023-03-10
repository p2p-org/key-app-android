package org.p2p.uikit.utils.skeleton

import androidx.annotation.Px
import android.view.Gravity
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.toPx

fun textCellSkeleton(
    @Px height: Int,
    @Px width: Int,
    @Px radius: Float = 0f,
    gravity: Int = Gravity.CENTER,
): TextViewCellModel.Skeleton = TextViewCellModel.Skeleton(
    roundedSkeleton(
        height = height,
        width = width,
        radius = radius,
        gravity = gravity,
    )
)

fun roundedSkeleton(
    @Px height: Int,
    @Px width: Int,
    @Px radius: Float = 0f,
    gravity: Int = Gravity.CENTER,
): SkeletonCellModel = SkeletonCellModel(
    height = height,
    width = width,
    radius = radius,
    gravity = gravity,
)
