package org.p2p.wallet.common.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import org.p2p.wallet.R
import org.p2p.wallet.utils.dip

class RoundedImageView @JvmOverloads constructor(
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
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView)
            radius = typedArray.getDimension(R.styleable.RoundedImageView_cornerRadius, radius)
            typedArray.recycle()
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
