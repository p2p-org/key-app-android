package org.p2p.uikit.sample

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import by.kirich1409.viewbindingdelegate.viewBinding
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.core.common.IconContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.icon_wrapper.IconWrapperUiModel
import org.p2p.uikit.components.finance_block.FinanceBlockUiModel
import org.p2p.uikit.components.finance_block.UiKitFinanceBlockView
import org.p2p.uikit.components.left_side.LeftSideUiModel
import org.p2p.uikit.components.right_side.RightSideUiModel
import org.p2p.uikit.databinding.ItemFinanceBlockBinding
import org.p2p.uikit.sample.databinding.FragmentFinanceBlockBinding
import org.p2p.uikit.utils.image.ImageViewUiModel
import org.p2p.uikit.utils.image.commonCircleImage
import org.p2p.uikit.utils.text.TextViewBackgroundUiModel
import org.p2p.uikit.utils.text.TextViewUiModel
import org.p2p.uikit.utils.toPx
import kotlin.jvm.internal.Intrinsics

class FinanceBlockFragment : Fragment(R.layout.fragment_finance_block) {

    private val binding by viewBinding(FragmentFinanceBlockBinding::bind)

    private val adapter = Adapter(
        DiffCallback(),
        financeBlockDelegate(
            inflateListener = { finBlock ->
                finBlock.setOnClickAction { view, item ->
                    val rootPayload = finBlock.item.payload
                    item.payload
                    view.binding.leftSideView
                }

                finBlock.binding.rightSideView.setOnSwitchAction { view, item, isChecked ->
                    // root payload
                    val rootPayload = finBlock.item.payload
                    // inner payload
                    // item.payload
                }
            }, onBindListener = { view, item ->

            }
        ),
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(view.context)
        binding.recyclerView.adapter = adapter

        val list = mutableListOf<Any>()

        fun model(left: LeftSideUiModel? = null) =
            FinanceBlockUiModel(left, null)

        fun model(right: RightSideUiModel? = null) =
            FinanceBlockUiModel(null, right)

        fun model(left: LeftSideUiModel? = null, right: RightSideUiModel? = null) =
            FinanceBlockUiModel(left, right)

        list.apply {
            add(model(leftSingleLine))
            add(model(leftDoubleLine))
            add(model(leftTripleLine))
            add(model(leftSingleLineWithoutImage))
            add(model(leftTripleLineWithoutImage))

            add(model(rightTwoLineSingleLine))
            add(model(rightTwoLineDoubleLine))
            add(model(rightSingleLineText))
            add(model(rightSingleLineTextIcon))
            add(model(rightSingleLineIcon))
            add(model(rightSingleLineTextTwoIcon))

            add(model(leftTripleCustomText, rightTextBadge))


            add(model(leftDoubleLine, rightTwoLineSingleLine))
            add(model(leftDoubleLine, rightTwoLineDoubleLine))
            add(model(leftDoubleLine, rightSingleLineText))
            add(model(leftDoubleLine, rightSingleLineTextIcon))
            add(model(leftDoubleLine, rightSingleLineIcon))
            add(model(leftDoubleLine, rightSingleLineTextTwoIcon))

            add(model(leftSingleLine, rightTwoLineSingleLine))
            add(model(leftDoubleLine, rightTwoLineDoubleLine))
            add(model(leftTripleLine, rightSingleLineText))
            add(model(leftSingleLineWithoutImage, rightSingleLineTextIcon))
            add(model(leftDoubleLine, rightSingleLineIcon))
            add(model(leftTripleLineWithoutImage, rightSingleLineTextTwoIcon))
        }

        adapter.items = list
    }
}

private class Adapter(
    diffCallback: DiffUtil.ItemCallback<Any>,
    vararg delegates: AdapterDelegate<List<Any>>,
) : AsyncListDifferDelegationAdapter<Any>(diffCallback, *delegates)

fun financeBlockDelegate(
    inflateListener: ((view: UiKitFinanceBlockView) -> Unit)? = null,
    onBindListener: ((view: UiKitFinanceBlockView, item: FinanceBlockUiModel) -> Unit)? = null,
) = adapterDelegateViewBinding<FinanceBlockUiModel, Any, ItemFinanceBlockBinding>(
    { layoutInflater, parent -> ItemFinanceBlockBinding.inflate(layoutInflater, parent, false) }
) {

    inflateListener?.invoke(binding.root)

    bind {
        binding.root.bind(item)
        onBindListener?.invoke(binding.root, item)
    }
}

private class DiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            else -> oldItem.javaClass == newItem.javaClass
        }
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            else -> Intrinsics.areEqual(oldItem, newItem)
        }
    }

    override fun getChangePayload(oldItem: Any, newItem: Any): Any? {
        return when {
            else -> super.getChangePayload(oldItem, newItem)
        }
    }
}

val leftSingleLine: LeftSideUiModel
    get() = LeftSideUiModel.IconWithText(
        icon = IconWrapperUiModel.SingleIcon(fullFit),
        firstLineText = TextViewUiModel(text = TextContainer.Raw("Send")),
    )

val leftDoubleLine: LeftSideUiModel
    get() = LeftSideUiModel.IconWithText(
        icon = IconWrapperUiModel.TwoIcon(
            fullFit,
            centerIcon
        ),
        firstLineText = TextViewUiModel(text = TextContainer.Raw("Send")),
        secondLineText = TextViewUiModel(text = TextContainer.Raw("23.8112 SOL")),
    )

val leftTripleLine: LeftSideUiModel
    get() = LeftSideUiModel.IconWithText(
        icon = IconWrapperUiModel.SingleIcon(centerIcon),
        firstLineText = TextViewUiModel(text = TextContainer.Raw("Send")),
        secondLineText = TextViewUiModel(text = TextContainer.Raw("23.8112 SOL")),
        thirdLineText = TextViewUiModel(text = TextContainer.Raw("23.8112 SOL")),
    )

val leftTripleCustomText: LeftSideUiModel
    get() = LeftSideUiModel.IconWithText(
        icon = IconWrapperUiModel.SingleIcon(centerIcon),
        firstLineText = TextViewUiModel(
            text = TextContainer.Raw("Send"),
            textAppearance = R.style.UiKit_TextAppearance_SemiBold_Text1,
            textColor = R.color.text_rose,
            textSizeSp = 24f,
        ),
        secondLineText = TextViewUiModel(
            text = TextContainer.Raw("23.8112 SOL"),
            textColor = R.color.text_electric,
        ),
        thirdLineText = TextViewUiModel(
            text = TextContainer.Raw("23.8112 SOL"),
            textColor = R.color.text_lime,
            textSizeSp = 9f,
        ),
    )

val leftSingleLineWithoutImage: LeftSideUiModel
    get() = LeftSideUiModel.IconWithText(
        firstLineText = TextViewUiModel(text = TextContainer.Raw("Send")),
    )

val leftTripleLineWithoutImage: LeftSideUiModel
    get() = LeftSideUiModel.IconWithText(
        firstLineText = TextViewUiModel(text = TextContainer.Raw("Send")),
        secondLineText = TextViewUiModel(text = TextContainer.Raw("23.8112 SOL")),
        thirdLineText = TextViewUiModel(text = TextContainer.Raw("23.8112 SOL")),
    )

val rightTwoLineSingleLine: RightSideUiModel
    get() = RightSideUiModel.TwoLineText(
        firstLineText = TextViewUiModel(text = TextContainer.Raw("$190.91")),
    )

val rightTwoLineDoubleLine: RightSideUiModel
    get() = RightSideUiModel.TwoLineText(
        firstLineText = TextViewUiModel(text = TextContainer.Raw("$190.91")),
        secondLineText = TextViewUiModel(text = TextContainer.Raw("23.8112 SOL")),
    )

val rightSingleLineText: RightSideUiModel
    get() = RightSideUiModel.SingleTextTwoIcon(
        text = TextViewUiModel(text = TextContainer.Raw("23.8112 SOL")),
    )

val rightSingleLineTextIcon: RightSideUiModel
    get() = RightSideUiModel.SingleTextTwoIcon(
        text = TextViewUiModel(text = TextContainer.Raw("23.8112 SOL")),
        firstIcon = ImageViewUiModel(
            icon = IconContainer.Res(R.drawable.ic_arrow_forward),
            iconTint = R.color.icons_mountain
        ),
    )

val rightSingleLineIcon: RightSideUiModel
    get() = RightSideUiModel.SingleTextTwoIcon(
        firstIcon = ImageViewUiModel(
            icon = IconContainer.Res(R.drawable.ic_arrow_forward),
            iconTint = R.color.icons_mountain
        ),
    )

val rightSingleLineTextTwoIcon: RightSideUiModel
    get() = RightSideUiModel.SingleTextTwoIcon(
        text = TextViewUiModel(text = TextContainer.Raw("23.8112 SOL")),
        firstIcon = ImageViewUiModel(
            icon = IconContainer.Res(R.drawable.ic_arrow_forward),
            iconTint = R.color.icons_mountain
        ),
        secondIcon = ImageViewUiModel(
            icon = IconContainer.Res(R.drawable.ic_info_outline),
            iconTint = R.color.icons_mountain
        ),
    )

val rightTextBadge: RightSideUiModel
    get() = RightSideUiModel.SingleTextTwoIcon(
        text = TextViewUiModel(
            text = TextContainer.Raw("23.8112 SOL"),
            badgeBackground = TextViewBackgroundUiModel(),
        ),
        firstIcon = ImageViewUiModel(
            icon = IconContainer.Res(R.drawable.ic_arrow_forward),
            iconTint = R.color.icons_mountain
        ),
        secondIcon = ImageViewUiModel(
            icon = IconContainer.Res(R.drawable.ic_info_outline),
            iconTint = R.color.icons_mountain
        ),
    )

val fullFit: ImageViewUiModel
    get() = commonCircleImage(
        icon = IconContainer.Res(R.drawable.shape_bottom_navigation_background),
        strokeWidth = 2f.toPx(),
        strokeColor = R.color.icons_grass,
    ).copy(
        iconTint = R.color.icons_night,
    )

val centerIcon: ImageViewUiModel
    get() = commonCircleImage(
        icon = IconContainer.Res(R.drawable.ic_arrow_forward),
        strokeWidth = 2f.toPx(),
        strokeColor = R.color.icons_electric,
    ).copy(
        iconTint = R.color.icons_night,
    )

