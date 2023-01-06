package org.p2p.uikit.atoms.skeleton

import androidx.core.view.isVisible
import android.content.Context
import android.util.AttributeSet
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetSkeletonLineBinding
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.inflateViewBinding

class UiKitSkeletonLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ShimmerFrameLayout(context, attrs, defStyleAttr) {

    private val binding = inflateViewBinding<WidgetSkeletonLineBinding>()
    private val shimmerBuilder = Shimmer.ColorHighlightBuilder()
        .setBaseAlpha(1f)
        .setBaseColor(binding.getColor(R.color.rain))
        .setHighlightAlpha(0f)
        .setHighlightColor(binding.getColor(R.color.snow))
        .setAutoStart(true)

    init {
        setShimmer(shimmerBuilder.build())
    }

    fun bind(model: UiKitSkeletonLineModel) = with(model) {
        binding.viewSkeletonLineImage.isVisible = isImageVisible
        binding.viewSkeletonLineFirstText.isVisible = isFirstTextLineVisible
        binding.viewSkeletonLineSecondText.isVisible = isSecondTextLineVisible
    }
}
