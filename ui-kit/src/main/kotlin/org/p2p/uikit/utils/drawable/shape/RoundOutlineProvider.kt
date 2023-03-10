package org.p2p.uikit.utils.drawable.shape

import android.graphics.Outline
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

class RoundOutlineProvider : ViewOutlineProvider() {

    private val rect = Rect()
    private var destination = RectF()

    private var shadowDrawable: MaterialShapeDrawable = MaterialShapeDrawable()

    fun update(newShape: ShapeAppearanceModel) {
        shadowDrawable.shapeAppearanceModel = newShape
    }

    override fun getOutline(view: View, outline: Outline) {
        destination.set(0f, 0f, view.width.toFloat(), view.height.toFloat())
        destination.round(rect)
        shadowDrawable.bounds = rect
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> shadowDrawable.getOutline(outline)
            else -> getOutline(outline)
        }
    }

    private fun getOutline(outline: Outline) = with(shadowDrawable.shapeAppearanceModel) {
        val left = rect.left
        val top = rect.top
        val right = rect.right
        val bottom = rect.bottom

        val topLeft = topLeftCornerSize.getCornerSize(destination).toInt()
        val topRight = topRightCornerSize.getCornerSize(destination).toInt()
        val bottomRight = bottomRightCornerSize.getCornerSize(destination).toInt()
        val bottomLeft = bottomLeftCornerSize.getCornerSize(destination).toInt()
        val cornerRadius = arrayOf(topLeft, topRight, bottomRight, bottomLeft)
            .filter { it != 0 }.maxOrNull()
        val cornerRadiusF = cornerRadius?.toFloat()
        when {
            // without corners
            cornerRadiusF == null -> outline.setRect(left, top, right, bottom)
            // all corners
            topLeft != 0 && topRight != 0 && bottomRight != 0 && bottomLeft != 0 ->
                outline.setRoundRect(left, top, right, bottom, cornerRadiusF)
            // top corners
            topLeft != 0 && topRight != 0 && bottomRight == 0 && bottomLeft == 0 ->
                outline.setRoundRect(left, top, right, bottom + cornerRadius, cornerRadiusF)
            // bottom corners
            topLeft == 0 && topRight == 0 && bottomRight != 0 && bottomLeft != 0 ->
                outline.setRoundRect(left, top - cornerRadius, right, bottom, cornerRadiusF)
            // left corners
            topLeft != 0 && topRight == 0 && bottomRight == 0 && bottomLeft != 0 ->
                outline.setRoundRect(left, top, right + cornerRadius, bottom, cornerRadiusF)
            // right corners
            topLeft == 0 && topRight != 0 && bottomRight != 0 && bottomLeft == 0 ->
                outline.setRoundRect(left - cornerRadius, top, right, bottom, cornerRadiusF)
            // top left corner
            topLeft != 0 && topRight == 0 && bottomRight == 0 && bottomLeft == 0 ->
                outline.setRoundRect(left, top, right + cornerRadius, bottom + cornerRadius, cornerRadiusF)
            // top right corner
            topLeft == 0 && topRight != 0 && bottomRight == 0 && bottomLeft == 0 ->
                outline.setRoundRect(left - cornerRadius, top, right, bottom + cornerRadius, cornerRadiusF)
            // bottom left corner
            topLeft == 0 && topRight == 0 && bottomRight == 0 && bottomLeft != 0 ->
                outline.setRoundRect(left, top - cornerRadius, right + cornerRadius, bottom, cornerRadiusF)
            // bottom right corner
            topLeft == 0 && topRight == 0 && bottomRight != 0 && bottomLeft == 0 ->
                outline.setRoundRect(left - cornerRadius, top - cornerRadius, right, bottom, cornerRadiusF)
        }
    }
}
