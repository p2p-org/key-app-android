package org.p2p.uikit.atoms

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.use
import org.p2p.uikit.R
import org.p2p.wallet.utils.dip

class UiKitRoundedImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_RADIUS_IN_DP = 12
    }

    private var path: Path = Path()
    private var radius = dip(DEFAULT_RADIUS_IN_DP).toFloat()

    init {
        attrs?.let {
            context.obtainStyledAttributes(attrs, R.styleable.UiKitRoundedImageView).use { typedArray ->
                radius = typedArray.getDimension(R.styleable.UiKitRoundedImageView_cornerRadius, radius)
            }

        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val rect = RectF(0f, 0f, this.width.toFloat(), this.height.toFloat())
        path.addRoundRect(rect, radius, radius, Path.Direction.CW)
        canvas.clipPath(path)

        super.onDraw(canvas)
    }
}
