package org.p2p.wallet.striga.signup.presetpicker

import androidx.annotation.StringRes
import android.content.res.Resources
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.finance_block.MainCellStyle
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.organisms.sectionheader.SectionHeaderCellModel
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.utils.toPx

class SelectItemPresenterCellMapper(
    private val resources: Resources
) {

    fun buildLoadingCellModels(): List<MainCellModel> {
        return buildList {
            repeat(5) {
                this += MainCellModel(
                    leftSideCellModel = itemLoadingSkeleton(),
                    styleType = MainCellStyle.BASE_CELL
                )
            }
        }
    }

    private fun itemLoadingSkeleton(): LeftSideCellModel {
        return LeftSideCellModel.IconWithText(
            firstLineText = TextViewCellModel.Skeleton(
                SkeletonCellModel(
                    height = 20.toPx(),
                    width = 94.toPx(),
                    radius = 4f.toPx(),
                )
            ),
            secondLineText = TextViewCellModel.Skeleton(
                SkeletonCellModel(
                    height = 20.toPx(),
                    width = 34.toPx(),
                    radius = 4f.toPx(),
                )
            )
        )
    }

    fun buildCellModels(
        items: List<SelectableItem>,
        selectedItem: SelectableItem?,
        itemName: SelectItemsStrings
    ): List<AnyCellItem> {
        return buildList {
            if (selectedItem != null) {
                this += buildHeaderCellItem(R.string.select_item_list_chosen, itemName.getSingularString(resources))
                this += selectedItem.mapItemToCellItem()
            }

            this += buildHeaderCellItem(R.string.select_item_list_all, itemName.getPluralString(resources))
            this += items
                .filter { it.id != selectedItem?.id }
                .map { it.mapItemToCellItem() }
        }
    }

    private fun buildHeaderCellItem(
        @StringRes headerPrefix: Int,
        headerPostfix: String
    ): SectionHeaderCellModel = SectionHeaderCellModel(
        sectionTitle = TextContainer.ResParams(headerPrefix, listOf(headerPostfix)),
        isShevronVisible = false,
        textColor = R.color.text_mountain,
        backgroundColor = R.color.bg_smoke,
        textAppearance = R.style.UiKit_TextAppearance_Regular_Caps
    )

    private fun SelectableItem.mapItemToCellItem(): MainCellModel {
        val titleCellModel: TextViewCellModel.Raw = TextViewCellModel.Raw(
            text = TextContainer(itemTitle),
            textAppearance = R.style.UiKit_TextAppearance_Regular_Text3,
            textColor = R.color.text_night
        )
        val subtitleCellModel: TextViewCellModel.Raw? = itemSubtitle?.let { subtitle ->
            TextViewCellModel.Raw(
                text = TextContainer(subtitle),
                textAppearance = R.style.UiKit_TextAppearance_Regular_Label1,
                textColor = R.color.text_mountain
            )
        }

        val iconOrEmoji: IconWrapperCellModel? = when {
            itemIcon != null -> IconWrapperCellModel.SingleIcon(ImageViewCellModel(itemIcon))
            itemEmoji != null -> IconWrapperCellModel.SingleEmoji(itemEmoji)
            else -> null
        }

        val leftSideCellModel = LeftSideCellModel.IconWithText(
            firstLineText = titleCellModel,
            secondLineText = subtitleCellModel,
            icon = iconOrEmoji
        )

        return MainCellModel(
            leftSideCellModel = leftSideCellModel,
            payload = this,
            styleType = MainCellStyle.BASE_CELL
        )
    }

    fun buildSearchCellModels(resultItems: List<SelectableItem>): List<AnyCellItem> =
        resultItems.map { it.mapItemToCellItem() }
}
