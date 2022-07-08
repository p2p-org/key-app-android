package org.p2p.uikit.atoms

import androidx.appcompat.widget.AppCompatImageView
import android.content.Context
import android.util.AttributeSet
import org.p2p.uikit.R

/**
 * Usage:
 * - width and height of this skeleton can be custom and is not pinned to concrete values
 * - can be drawn with rounded corners or not, default: false
 * ```
 *     <org.p2p.uikit.atoms.UiKitSkeletonRectangle
 *       android:layout_width="100dp"
 *       android:layout_height="10dp"
 *       app:isRounded="true|false" />
 * ```
 */
class UiKitSkeletonRectangle(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        context.obtainStyledAttributes(attrs, R.styleable.UiKitSkeletonRectangle).use { typedArray ->
            val isSkeletonRounded = typedArray.getBoolean(R.styleable.UiKitSkeletonRectangle_isRounded, false)

            setBackgroundResource(
                if (isSkeletonRounded) R.drawable.bg_skeleton_rectangle_rounded else R.drawable.bg_skeleton_rectangle
            )
        }
    }
}
