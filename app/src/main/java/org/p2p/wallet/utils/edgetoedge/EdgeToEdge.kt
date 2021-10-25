package org.p2p.wallet.utils.edgetoedge

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.Space
import androidx.core.view.ScrollingView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.p2p.wallet.R
import java.lang.ref.WeakReference
import java.util.WeakHashMap

@DslMarker
annotation class EdgeToEdgeDsl

@EdgeToEdgeDsl
class EdgeToEdgeBuilder(
    private val rootView: View,
    private val isTopFragmentProvider: () -> Boolean
) {

    private val edgeToEdge: EdgeToEdge =
        rootView.getTag(R.id.edgetoedge) as? EdgeToEdge
            ?: EdgeToEdge().also { rootView.setTag(R.id.edgetoedge, it) }

    /**
     * Fits the view to the returned [Edge] of the screen by adjusting its `padding`,
     * `margin` or `height`. The function detects the type of [Adjustment] and whether
     * `clipToPadding` should be applied to the view as following.
     *
     * - for [android.widget.Space] the adjustment is [Adjustment.Height]
     * - for [android.widget.Button] or [android.widget.ImageButton] the adjustment
     * is [Adjustment.Margin]
     * - for any other widget the adjustment is [Adjustment.Padding]
     * - `clipToPadding` is disabled for the [android.widget.ScrollView] and instances
     * of [androidx.core.view.ScrollingView], and not changed for all the other widgets.
     *
     * Default values can be overridden inside the `block` lambda function.
     */
    fun View.fit(block: FittingBuilder.() -> Edge) {
        FittingBuilder(
            adjustment = detectAdjustment(),
            clipToPadding = detectClipToPadding()
        ).also { builder ->
            val edge = builder.block()
            val adjustment = builder.adjustment
            val clipToPadding = builder.clipToPadding
            verifyEdgeAdjustment(edge, adjustment)
            applyClipToPadding(clipToPadding)
            edgeToEdge.fittings[this] = createFitting(edge, adjustment, clipToPadding)
        }
    }

    /** Same as [fit] but overrides default adjustment to [Adjustment.Padding]. */
    inline fun View.fitPadding(crossinline block: FittingBuilder.() -> Edge) {
        fit {
            adjustment = Adjustment.Padding
            block()
        }
    }

    /** Same as [fit] but overrides default adjustment to [Adjustment.Margin]. */
    inline fun View.fitMargin(crossinline block: FittingBuilder.() -> Edge) {
        fit {
            adjustment = Adjustment.Margin
            block()
        }
    }

    /** Same as [fit] but overrides default adjustment to [Adjustment.Height]. */
    inline fun View.fitHeight(crossinline block: FittingBuilder.() -> Edge) {
        fit {
            adjustment = Adjustment.Height
            block()
        }
    }

    /** Same as [fit] but overrides default adjustment to [Adjustment.Width]. */
    inline fun View.fitWidth(crossinline block: FittingBuilder.() -> Edge) {
        fit {
            adjustment = Adjustment.Width
            block()
        }
    }

    /** Removes fitting rule for the view. */
    fun View.unfit() {
        edgeToEdge.fittings.remove(this)
    }

    @PublishedApi
    internal fun build() {
        if (!edgeToEdge.listening) {
            edgeToEdge.listening = true
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
                if (insets.isInsetConsumed() || !isTopFragmentProvider.invoke()) {
                    return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
                }
                WindowInsetsCompat(insets).let {
                    edgeToEdge.insets = it
                    edgeToEdge.applyFittings(it)
                }
                WindowInsetsCompat.CONSUMED
            }
        }
    }
}

@EdgeToEdgeDsl
class FittingBuilder(
    var adjustment: Adjustment,
    var clipToPadding: Boolean?,
)

private const val FLAG_LEFT = 1
private const val FLAG_TOP = 1.shl(1)
private const val FLAG_RIGHT = 1.shl(2)
private const val FLAG_BOTTOM = 1.shl(3)

sealed class Edge(
    internal val flags: Int,
) {
    object Left : Edge(FLAG_LEFT)
    object Top : Edge(FLAG_TOP)
    object Right : Edge(FLAG_RIGHT)
    object Bottom : Edge(FLAG_BOTTOM)

    /** Shortcut for [Edge.Left] + [Edge.Top] + [Edge.Right] + [Edge.Bottom] */
    object All : Edge(FLAG_LEFT + FLAG_TOP + FLAG_RIGHT + FLAG_BOTTOM)

    /** Shortcut for [Edge.Left] + [Edge.Top] + [Edge.Right] */
    object TopArc : Edge(FLAG_LEFT + FLAG_TOP + FLAG_RIGHT)

    /** Shortcut for [Edge.Left] + [Edge.Bottom] + [Edge.Right] */
    object BottomArc : Edge(FLAG_LEFT + FLAG_BOTTOM + FLAG_RIGHT)

    internal class CompositeEdge(edges: Int) : Edge(edges)

    operator fun plus(edge: Edge): Edge =
        CompositeEdge(this.flags + edge.flags)
}

enum class Adjustment { Padding, Margin, Height, Width }

private data class Fitting(
    val view: WeakReference<View>,
    val adjustment: Adjustment,
    val edge: Edge,
    val clipToPadding: Boolean?,
    val paddingLeft: Int,
    val paddingTop: Int,
    val paddingRight: Int,
    val paddingBottom: Int,
    val marginLeft: Int,
    val marginTop: Int,
    val marginRight: Int,
    val marginBottom: Int,
)

private data class EdgeToEdge(
    val fittings: WeakHashMap<View, Fitting> = WeakHashMap(),
    var listening: Boolean = false,
    var insets: WindowInsetsCompat? = null,
)

internal fun View.dispatchWindowInsets() {
    if (isAttachedToWindow) ViewCompat.requestApplyInsets(this)
    else addOnAttachStateChangeListener(
        object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(view: View) {}
            override fun onViewAttachedToWindow(view: View) {
                view.removeOnAttachStateChangeListener(this)
                ViewCompat.requestApplyInsets(view)
            }
        }
    )
}

private fun View.verifyEdgeAdjustment(edge: Edge, adjustment: Adjustment) {
    if (adjustment == Adjustment.Height && edge is Edge.CompositeEdge) {
        val edges = StringBuilder()
        if (edge.flags and FLAG_LEFT > 0) edges.append(", Left")
        if (edge.flags and FLAG_TOP > 0) edges.append(", Top")
        if (edge.flags and FLAG_RIGHT > 0) edges.append(", Right")
        if (edge.flags and FLAG_BOTTOM > 0) edges.append(", Bottom")
        throw IllegalArgumentException(
            "Height adjustment can only be applied to a single edge." +
                " Actual edges: ${edges.substring(2)}, View: $this"
        )
    }
}

private fun EdgeToEdge.applyFittings(insets: WindowInsetsCompat) {
    for (fitting in fittings.values) {
        val view = fitting.view.get() ?: continue
        with(fitting) {
            when (adjustment) {
                Adjustment.Padding -> applyInsetsAsPadding(insets, view, edge.flags)
                Adjustment.Margin -> applyInsetsAsMargin(insets, view, edge.flags)
                Adjustment.Height -> applyInsetsAsHeight(insets, view, edge.flags)
                Adjustment.Width -> applyInsetsAsWidth(insets, view, edge.flags)
            }
        }
    }
}

@Suppress("ComplexCondition")
private fun Fitting.applyInsetsAsPadding(insets: WindowInsetsCompat, view: View, flags: Int) {
    val left = if (flags and FLAG_LEFT > 0) paddingLeft + insets.leftBarInset() else view.paddingLeft
    val top = if (flags and FLAG_TOP > 0) paddingTop + insets.statusBarInset() else view.paddingTop
    val right = if (flags and FLAG_RIGHT > 0) paddingRight + insets.rightBarInset() else view.paddingRight
    val bottom = if (flags and FLAG_BOTTOM > 0) paddingBottom + insets.navigationBarInset() else view.paddingBottom

    if (view.paddingLeft != left ||
        view.paddingTop != top ||
        view.paddingRight != right ||
        view.paddingBottom != bottom
    ) {
        view.setPadding(left, top, right, bottom)
    }
}

@Suppress("ComplexCondition")
private fun Fitting.applyInsetsAsMargin(insets: WindowInsetsCompat, view: View, flags: Int) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    val left = if (flags and FLAG_LEFT > 0) marginLeft + insets.leftBarInset() else layoutParams.leftMargin
    val top = if (flags and FLAG_TOP > 0) marginTop + insets.statusBarInset() else layoutParams.topMargin
    val right = if (flags and FLAG_RIGHT > 0) marginRight + insets.rightBarInset() else layoutParams.rightMargin
    val bottom =
        if (flags and FLAG_BOTTOM > 0) marginBottom + insets.navigationBarInset() else layoutParams.bottomMargin

    if (left != layoutParams.leftMargin ||
        top != layoutParams.topMargin ||
        right != layoutParams.rightMargin ||
        bottom != layoutParams.bottomMargin
    ) {
        layoutParams.leftMargin = left
        layoutParams.topMargin = top
        layoutParams.rightMargin = right
        layoutParams.bottomMargin = bottom
        view.layoutParams = layoutParams
    }
}

private fun applyInsetsAsHeight(insets: WindowInsetsCompat, view: View, flags: Int) {
    val height = when (flags) {
        FLAG_LEFT -> insets.leftBarInset()
        FLAG_TOP -> insets.statusBarInset()
        FLAG_RIGHT -> insets.rightBarInset()
        FLAG_BOTTOM -> insets.navigationBarInset()
        else -> error("Unexpected edge flags: $flags")
    }
    if (view.height != height) {
        val layoutParams = view.layoutParams
        layoutParams.height = height
        view.layoutParams = layoutParams
    }
}

private fun applyInsetsAsWidth(insets: WindowInsetsCompat, view: View, flags: Int) {
    val width = when (flags) {
        FLAG_LEFT -> insets.leftBarInset()
        FLAG_TOP -> insets.statusBarInset()
        FLAG_RIGHT -> insets.rightBarInset()
        FLAG_BOTTOM -> insets.navigationBarInset()
        else -> error("Unexpected edge flags: $flags")
    }
    if (view.width != width) {
        val layoutParams = view.layoutParams
        layoutParams.width = View.MeasureSpec.makeMeasureSpec(
            width, View.MeasureSpec.EXACTLY
        )
        view.layoutParams = layoutParams
    }
}

private fun View.detectAdjustment(): Adjustment =
    when {
        this is Space -> Adjustment.Height
        (this is Button || this is ImageButton) &&
            layoutParams is ViewGroup.MarginLayoutParams -> Adjustment.Margin
        else -> Adjustment.Padding
    }

private fun View.detectClipToPadding(): Boolean? =
    if (this is ScrollView || this is ScrollingView && this is ViewGroup) false else null

private fun View.createFitting(
    edge: Edge,
    adjustment: Adjustment,
    clipToPadding: Boolean?,
): Fitting {
    val layoutMargin = layoutParams as? ViewGroup.MarginLayoutParams
    return Fitting(
        view = WeakReference(this),
        edge = edge,
        adjustment = adjustment,
        clipToPadding = clipToPadding,
        paddingLeft = paddingLeft,
        paddingTop = paddingTop,
        paddingRight = paddingRight,
        paddingBottom = paddingBottom,
        marginLeft = layoutMargin?.leftMargin ?: 0,
        marginTop = layoutMargin?.topMargin ?: 0,
        marginRight = layoutMargin?.rightMargin ?: 0,
        marginBottom = layoutMargin?.bottomMargin ?: 0
    )
}

private fun View.applyClipToPadding(clipToPadding: Boolean?) {
    clipToPadding?.let {
        check(this is ViewGroup) {
            "'clipToPadding' can only be applied to a ViewGroup, actual: $this"
        }
        this.clipToPadding = it
    }
}

fun WindowInsetsCompat.statusBarInset(): Int {
    return getInsets(WindowInsetsCompat.Type.systemBars()).top
}

fun WindowInsetsCompat.navigationBarInset(): Int {
    val navigation = getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
    return (if (imeInset() == 0) navigation else 0) + imeInset()
}

fun WindowInsetsCompat.leftBarInset(): Int {
    return getInsets(WindowInsetsCompat.Type.captionBar()).left
}

fun WindowInsetsCompat.rightBarInset(): Int {
    return getInsets(WindowInsetsCompat.Type.captionBar()).right
}

fun WindowInsetsCompat.imeInset(): Int {
    return getInsets(WindowInsetsCompat.Type.ime()).bottom
}

fun WindowInsetsCompat.systemInset(): Int {
    return getInsets(WindowInsetsCompat.Type.systemBars()).bottom
}

fun WindowInsetsCompat.isInsetConsumed(): Boolean {
    val statusBarHeight = getInsets(WindowInsetsCompat.Type.statusBars()).top
    val navBarHeight = getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
    return statusBarHeight == 0 && navBarHeight == 0
}