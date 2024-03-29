package org.p2p.uikit.atoms

import androidx.appcompat.widget.AppCompatImageView
import android.content.Context
import android.util.AttributeSet
import org.p2p.uikit.R

/**
 * Usage:
 * - to set width and height of this Skeleton use special @dimen values
 */
class UiKitSkeletonCircle(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        setBackgroundResource(R.drawable.bg_skeleton_circle)
    }
}
