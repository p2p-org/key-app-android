package org.p2p.uikit.atoms.skeleton

import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import android.content.Context
import android.util.AttributeSet
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import org.p2p.uikit.R
import org.p2p.uikit.databinding.AtomSkeletonLineViewBinding
import org.p2p.uikit.utils.inflateViewBinding

class UiKitSkeletonLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ShimmerFrameLayout(context, attrs, defStyleAttr) {

    private val binding = inflateViewBinding<AtomSkeletonLineViewBinding>()
    private val shimmerBuilder = Shimmer.ColorHighlightBuilder()
        .setBaseAlpha(1f)
        .setBaseColor(ContextCompat.getColor(context, R.color.rain))
        .setHighlightAlpha(0f)
        .setHighlightColor(ContextCompat.getColor(context, R.color.snow))
        .setAutoStart(true)

    init {
        setShimmer(shimmerBuilder.build())
    }

    fun bind(model: UiKitSkeletonLineViewModel) = with(model) {
        binding.skeletonLineImage.isVisible = showImage
        binding.skeletonLineFirstText.isVisible = showFirstTextLine
        binding.skeletonLineSecondText.isVisible = showSecondTextLine
    }
}
