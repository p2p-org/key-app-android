package org.p2p.uikit.components

import android.content.Context
import android.graphics.Color
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import org.p2p.core.common.TextContainer
import org.p2p.uikit.R
import org.p2p.uikit.databinding.ViewInformerViewBinding
import org.p2p.uikit.utils.SpanUtils
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.image.bind
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.bindOrGone

class UiKitInformerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = inflateViewBinding<ViewInformerViewBinding>()

    fun bind(model: InformerViewCellModel) = with(binding) {
        imageViewLeftIcon.bind(model.leftIcon.iconCellModel())
        textViewTitle.bindOrGone(model.title?.titleCellModel())
        textViewCaption.bindOrGone(model.captionCellModel())
        textViewCaption.movementMethod = LinkMovementMethod()
        textViewCaption.highlightColor = Color.TRANSPARENT
        if (model.infoLine?.position == InformerViewCellModel.InfoLineParams.InfoLinePosition.BOTTOM) {
            textViewInfoLine.bindOrGone(model.infoLine.infoLineCellModel())
            model.infoLine.onInfoLineClicked?.also { listener ->
                textViewInfoLine.setOnClickListener {
                    listener.invoke(model.infoLine.value.getString(context))
                }
            }
        }

        model.onViewClicked?.also { listener ->
            setOnClickListener { listener(model) }
        }

    }

    private fun InformerViewCellModel.LeftIconParams.iconCellModel(): ImageViewCellModel {
        return ImageViewCellModel(
            icon = icon,
            iconTint = iconTint,
            background = DrawableCellModel(
                drawable = shapeDrawable(shapeCircle()),
                tint = R.color.bg_cloud
            )
        )
    }

    private fun InformerViewCellModel.TitleParams.titleCellModel(): TextViewCellModel.Raw? {
        return TextViewCellModel.Raw(
            text = value,
            textAppearance = R.style.UiKit_TextAppearance_SemiBold_Text1,
            textColor = R.color.text_night,
            drawable = titleIcon,
            drawableTint = titleIconTint,
            drawableGravity = Gravity.RIGHT
        )
    }

    private fun InformerViewCellModel.captionCellModel(): TextViewCellModel.Raw? {
        return caption?.let {
            val captionText = it.getString(context)
            val resultCaptionText =
                if (infoLine?.position == InformerViewCellModel.InfoLineParams.InfoLinePosition.CAPTION_LINE) {
                    buildCaptionPlusInfoLine(captionText, infoLine)
                } else {
                    captionText
                }

            TextViewCellModel.Raw(
                text = TextContainer(resultCaptionText),
                textAppearance = R.style.UiKit_TextAppearance_Regular_Text4,
                textColor = R.color.text_night,
            )
        }
    }

    private fun buildCaptionPlusInfoLine(
        caption: String,
        infoLineParams: InformerViewCellModel.InfoLineParams
    ): CharSequence {
        val infoLineText = infoLineParams.value.getString(context)
        val fullCaptionText = "$caption $infoLineText"
        return infoLineParams.onInfoLineClicked?.let { listener ->
            SpanUtils.highlightLinkNoUnderline(
                commonText = fullCaptionText,
                highlightedText = infoLineText,
                color = getColor(infoLineParams.textColorRes),
                onClick = { listener(infoLineText) }
            )
        } ?: SpanUtils.highlightText(
            commonText = fullCaptionText,
            highlightedText = infoLineText,
            color = getColor(infoLineParams.textColorRes),
        )
    }

    private fun InformerViewCellModel.InfoLineParams.infoLineCellModel(): TextViewCellModel {
        return TextViewCellModel.Raw(
            text = value,
            textAppearance = R.style.UiKit_TextAppearance_Regular_Text4,
            textColor = textColorRes,
        )
    }
}
