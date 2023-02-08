package org.p2p.uikit.atoms

import androidx.core.content.res.use
import android.content.Context
import android.util.AttributeSet
import com.google.android.material.imageview.ShapeableImageView
import org.p2p.core.common.setIcon
import org.p2p.uikit.R
import org.p2p.uikit.model.ImageViewUiModel
import org.p2p.uikit.utils.dip
import org.p2p.uikit.utils.getColorStateList
import org.p2p.uikit.utils.toPx

/**
 * app:cornerRadius - corner radius
 * doesn't work in design mode
 */
class UiKitRoundedImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ShapeableImageView(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_RADIUS_IN_DP = 12
    }

    private var radius = dip(DEFAULT_RADIUS_IN_DP).toFloat()

    init {
        attrs?.let {
            context.obtainStyledAttributes(attrs, R.styleable.UiKitRoundedImageView).use { typedArray ->
                radius = typedArray.getDimension(R.styleable.UiKitRoundedImageView_cornerRadius, radius)
            }
        }
        shapeAppearanceModel = shapeAppearanceModel
            .toBuilder()
            .setAllCornerSizes(radius)
            .build()
    }

    fun bind(model: ImageViewUiModel) {
        setIcon(model.icon)
        scaleType = model.scaleType
        model.background?.let { background = it }
        model.backgroundTint?.let { backgroundTintList = getColorStateList(it) }
        model.shape?.let { shapeAppearanceModel = it }
        model.strokeWidthDp?.let { strokeWidth = it.toFloat().toPx() }
        model.strokeColor?.let { setStrokeColorResource(it) }
    }
}
