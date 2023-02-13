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
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.finance_block.UiKitFinanceBlockView
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.databinding.ItemFinanceBlockBinding
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.sample.databinding.FragmentFinanceBlockBinding
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.image.commonCircleImage
import org.p2p.uikit.utils.text.TextViewBackgroundModel
import org.p2p.uikit.utils.text.TextViewCellModel
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

        val list = mutableListOf<AnyCellItem>()

        fun model(left: LeftSideCellModel? = null) =
            FinanceBlockCellModel(left, null)

        fun model(right: RightSideCellModel? = null) =
            FinanceBlockCellModel(null, right)

        fun model(left: LeftSideCellModel? = null, right: RightSideCellModel? = null) =
            FinanceBlockCellModel(left, right)

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
    diffCallback: DiffUtil.ItemCallback<AnyCellItem>,
    vararg delegates: AdapterDelegate<List<AnyCellItem>>,
) : AsyncListDifferDelegationAdapter<AnyCellItem>(diffCallback, *delegates)

fun financeBlockDelegate(
    inflateListener: ((view: UiKitFinanceBlockView) -> Unit)? = null,
    onBindListener: ((view: UiKitFinanceBlockView, item: FinanceBlockCellModel) -> Unit)? = null,
) = adapterDelegateViewBinding<FinanceBlockCellModel, AnyCellItem, ItemFinanceBlockBinding>(
    { layoutInflater, parent -> ItemFinanceBlockBinding.inflate(layoutInflater, parent, false) }
) {

    inflateListener?.invoke(binding.root)

    bind {
        binding.root.bind(item)
        onBindListener?.invoke(binding.root, item)
    }
}

private class DiffCallback : DiffUtil.ItemCallback<AnyCellItem>() {
    override fun areItemsTheSame(oldItem: AnyCellItem, newItem: AnyCellItem): Boolean {
        return when {
            else -> oldItem.javaClass == newItem.javaClass
        }
    }

    override fun areContentsTheSame(oldItem: AnyCellItem, newItem: AnyCellItem): Boolean {
        return when {
            else -> Intrinsics.areEqual(oldItem, newItem)
        }
    }

    override fun getChangePayload(oldItem: AnyCellItem, newItem: AnyCellItem): Any? {
        return when {
            else -> super.getChangePayload(oldItem, newItem)
        }
    }
}

val leftSingleLine: LeftSideCellModel
    get() = LeftSideCellModel.IconWithText(
        icon = IconWrapperCellModel.SingleIcon(fullFit),
        firstLineText = TextViewCellModel(text = TextContainer.Raw("Send")),
    )

val leftDoubleLine: LeftSideCellModel
    get() = LeftSideCellModel.IconWithText(
        icon = IconWrapperCellModel.TwoIcon(
            fullFit,
            centerIcon
        ),
        firstLineText = TextViewCellModel(text = TextContainer.Raw("Send")),
        secondLineText = TextViewCellModel(text = TextContainer.Raw("23.8112 SOL")),
    )

val leftTripleLine: LeftSideCellModel
    get() = LeftSideCellModel.IconWithText(
        icon = IconWrapperCellModel.SingleIcon(centerIcon),
        firstLineText = TextViewCellModel(text = TextContainer.Raw("Send")),
        secondLineText = TextViewCellModel(text = TextContainer.Raw("23.8112 SOL")),
        thirdLineText = TextViewCellModel(text = TextContainer.Raw("23.8112 SOL")),
    )

val leftTripleCustomText: LeftSideCellModel
    get() = LeftSideCellModel.IconWithText(
        icon = IconWrapperCellModel.SingleIcon(centerIcon),
        firstLineText = TextViewCellModel(
            text = TextContainer.Raw("Send"),
            textAppearance = R.style.UiKit_TextAppearance_SemiBold_Text1,
            textColor = R.color.text_rose,
            textSizeSp = 24f,
        ),
        secondLineText = TextViewCellModel(
            text = TextContainer.Raw("23.8112 SOL"),
            textColor = R.color.text_electric,
        ),
        thirdLineText = TextViewCellModel(
            text = TextContainer.Raw("23.8112 SOL"),
            textColor = R.color.text_lime,
            textSizeSp = 9f,
        ),
    )

val leftSingleLineWithoutImage: LeftSideCellModel
    get() = LeftSideCellModel.IconWithText(
        firstLineText = TextViewCellModel(text = TextContainer.Raw("Send")),
    )

val leftTripleLineWithoutImage: LeftSideCellModel
    get() = LeftSideCellModel.IconWithText(
        firstLineText = TextViewCellModel(text = TextContainer.Raw("Send")),
        secondLineText = TextViewCellModel(text = TextContainer.Raw("23.8112 SOL")),
        thirdLineText = TextViewCellModel(text = TextContainer.Raw("23.8112 SOL")),
    )

val rightTwoLineSingleLine: RightSideCellModel
    get() = RightSideCellModel.TwoLineText(
        firstLineText = TextViewCellModel(text = TextContainer.Raw("$190.91")),
    )

val rightTwoLineDoubleLine: RightSideCellModel
    get() = RightSideCellModel.TwoLineText(
        firstLineText = TextViewCellModel(text = TextContainer.Raw("$190.91")),
        secondLineText = TextViewCellModel(text = TextContainer.Raw("23.8112 SOL")),
    )

val rightSingleLineText: RightSideCellModel
    get() = RightSideCellModel.SingleTextTwoIcon(
        text = TextViewCellModel(text = TextContainer.Raw("23.8112 SOL")),
    )

val rightSingleLineTextIcon: RightSideCellModel
    get() = RightSideCellModel.SingleTextTwoIcon(
        text = TextViewCellModel(text = TextContainer.Raw("23.8112 SOL")),
        firstIcon = ImageViewCellModel(
            icon = DrawableContainer(R.drawable.ic_arrow_forward),
            iconTint = R.color.icons_mountain
        ),
    )

val rightSingleLineIcon: RightSideCellModel
    get() = RightSideCellModel.SingleTextTwoIcon(
        firstIcon = ImageViewCellModel(
            icon = DrawableContainer(R.drawable.ic_arrow_forward),
            iconTint = R.color.icons_mountain
        ),
    )

val rightSingleLineTextTwoIcon: RightSideCellModel
    get() = RightSideCellModel.SingleTextTwoIcon(
        text = TextViewCellModel(text = TextContainer.Raw("23.8112 SOL")),
        firstIcon = ImageViewCellModel(
            icon = DrawableContainer(R.drawable.ic_arrow_forward),
            iconTint = R.color.icons_mountain
        ),
        secondIcon = ImageViewCellModel(
            icon = DrawableContainer(R.drawable.ic_info_outline),
            iconTint = R.color.icons_mountain
        ),
    )

val rightTextBadge: RightSideCellModel
    get() = RightSideCellModel.SingleTextTwoIcon(
        text = TextViewCellModel(
            text = TextContainer.Raw("23.8112 SOL"),
            badgeBackground = TextViewBackgroundModel(),
        ),
        firstIcon = ImageViewCellModel(
            icon = DrawableContainer(R.drawable.ic_arrow_forward),
            iconTint = R.color.icons_mountain
        ),
        secondIcon = ImageViewCellModel(
            icon = DrawableContainer(R.drawable.ic_info_outline),
            iconTint = R.color.icons_mountain
        ),
    )

val fullFit: ImageViewCellModel
    get() = commonCircleImage(
        icon = DrawableContainer(R.drawable.shape_bottom_navigation_background),
        strokeWidth = 2f.toPx(),
        strokeColor = R.color.icons_grass,
    ).copy(
        iconTint = R.color.icons_night,
    )

val centerIcon: ImageViewCellModel
    get() = commonCircleImage(
        icon = DrawableContainer(R.drawable.ic_arrow_forward),
        strokeWidth = 2f.toPx(),
        strokeColor = R.color.icons_electric,
    ).copy(
        iconTint = R.color.icons_night,
    )

