package org.p2p.uikit.atoms

import androidx.appcompat.widget.AppCompatImageView
import android.content.Context
import android.util.AttributeSet
import org.p2p.uikit.R

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
