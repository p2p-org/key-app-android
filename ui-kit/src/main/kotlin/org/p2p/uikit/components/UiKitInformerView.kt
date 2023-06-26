package org.p2p.uikit.components

import androidx.core.content.res.use
import androidx.core.view.isVisible
import android.content.Context
import android.graphics.Color
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.R
import org.p2p.uikit.components.InformerViewCellModel.InfoLineParams.InfoLinePosition
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
import org.p2p.uikit.utils.text.bind
import org.p2p.uikit.utils.text.bindOrGone

class UiKitInformerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = inflateViewBinding<ViewInformerViewBinding>()

    init {
        context.obtainStyledAttributes(attrs, R.styleable.UiKitInformerView).use { style ->
            val leftIconRes: Int = style.getResourceId(R.styleable.UiKitInformerView_leftIcon, R.drawable.ic_checkbox_checked)
            val leftIconTint: Int? = style.getResourceId(R.styleable.UiKitInformerView_leftIconTint, -1)
                .takeIf { it != -1 }
            val title: String? = style.getString(R.styleable.UiKitInformerView_title)
            val caption: String? = style.getString(R.styleable.UiKitInformerView_caption)
            val infoLine: String? = style.getString(R.styleable.UiKitInformerView_infoLine)
            val infoLinePosition = style.getInt(R.styleable.UiKitInformerView_infoLinePosition, -1)
                .takeIf { it != -1 }
                ?.let { InfoLinePosition.values()[it] }

            val leftIconParams = if (leftIconTint != null) {
                InformerViewCellModel.LeftIconParams(
                    DrawableContainer(iconRes = leftIconRes),
                    iconTint = leftIconTint
                )
            } else {
                InformerViewCellModel.LeftIconParams(leftIconRes)
            }

            InformerViewCellModel(
                leftIcon = leftIconParams,
                title = title?.let { InformerViewCellModel.TitleParams(TextContainer(it)) },
                caption = caption?.let { TextContainer(it) },
                infoLine = infoLine?.let {
                    InformerViewCellModel.InfoLineParams(
                        TextContainer(text = it),
                        position = infoLinePosition!!
                    )
                }
            )
                .also(::bind)
        }
    }

    fun bind(model: InformerViewCellModel) = with(binding) {
        imageViewLeftIcon.bind(model.leftIcon.iconCellModel())
        textViewTitle.bindOrGone(model.title?.titleCellModel())
        textViewCaption.bindOrGone(model.captionCellModel())
        bindInfoLine(model)

        model.onViewClicked?.also { listener ->
            setOnClickListener { listener(model) }
        }
    }

    private fun bindInfoLine(model: InformerViewCellModel) = with(binding){
        when (model.infoLine?.position) {
            InfoLinePosition.BOTTOM -> {
                textViewInfoLine.bind(model.infoLine.infoLineCellModel())
                textViewInfoLine.setOnClickListener {
                    model.infoLine.onInfoLineClicked?.invoke(model.infoLine.value.getString(context))
                }
            }
            InfoLinePosition.CAPTION_LINE -> {
                requireNotNull(model.caption) { "You can't put an info line on caption if there no caption" }
                textViewCaption.movementMethod = LinkMovementMethod()
                textViewCaption.highlightColor = Color.TRANSPARENT

                textViewCaption.bind(captionPlusInfoLineCellModel(model.caption, model.infoLine))
                textViewInfoLine.isVisible = false
            }
            null -> {
                textViewInfoLine.isVisible = false
            }
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

    private fun InformerViewCellModel.TitleParams.titleCellModel(): TextViewCellModel.Raw {
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
            TextViewCellModel.Raw(
                text = it,
                textAppearance = R.style.UiKit_TextAppearance_Regular_Text4,
                textColor = R.color.text_night,
            )
        }
    }

    private fun captionPlusInfoLineCellModel(
        caption: TextContainer,
        infoLineParams: InformerViewCellModel.InfoLineParams
    ): TextViewCellModel.Raw {
        val resultCaption = buildCaptionPlusInfoLine(caption, infoLineParams)
        return TextViewCellModel.Raw(
            text = TextContainer(resultCaption),
            textAppearance = R.style.UiKit_TextAppearance_Regular_Text4,
            textColor = R.color.text_night,
        )
    }

    private fun buildCaptionPlusInfoLine(
        caption: TextContainer,
        infoLineParams: InformerViewCellModel.InfoLineParams
    ): CharSequence {
        val infoLineText = infoLineParams.value.getString(context)
        val captionText = caption.getString(context)
        val fullCaptionText = "$captionText $infoLineText"
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
