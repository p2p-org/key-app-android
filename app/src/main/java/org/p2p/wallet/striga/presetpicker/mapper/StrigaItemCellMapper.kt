package org.p2p.wallet.striga.presetpicker.mapper

import androidx.annotation.StringRes
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.organisms.sectionheader.SectionHeaderCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.striga.presetpicker.interactor.StrigaPresetDataItem
import org.p2p.wallet.striga.signup.model.StrigaOccupation
import org.p2p.wallet.striga.signup.model.StrigaSourceOfFunds

class StrigaItemCellMapper {

    fun buildCellModels(items: List<StrigaPresetDataItem>, selectedItem: StrigaPresetDataItem?): List<AnyCellItem> {
        val itemsWithoutSelected = items.filter { it != selectedItem }
        return buildList {
            if (selectedItem != null) {
                this += buildHeaderCellItem(getSelectedItemHeaderTitle(selectedItem))
                this += mapItemToCellItem(selectedItem)
            }
            if (itemsWithoutSelected.isNotEmpty()) {
                this += buildHeaderCellItem(getAllItemHeaderTitle(itemsWithoutSelected.first()))
                this += itemsWithoutSelected.map { mapItemToCellItem(it) }
            }
        }
    }

    private fun buildHeaderCellItem(@StringRes header: Int): SectionHeaderCellModel = SectionHeaderCellModel(
        sectionTitle = TextContainer(header),
        isShevronVisible = false,
        textColor = R.color.text_mountain,
        backgroundColor = R.color.bg_smoke,
        textAppearance = R.style.UiKit_TextAppearance_Regular_Caps
    )

    private fun mapItemToCellItem(item: StrigaPresetDataItem): FinanceBlockCellModel {
        return when (item) {
            is StrigaPresetDataItem.StrigaCountryItem -> item.details.mapItemToCellItem()
            is StrigaPresetDataItem.StrigaSourceOfFundsItem -> item.details.mapItemToCellItem()
            is StrigaPresetDataItem.StrigaOccupationItem -> item.details.mapItemToCellItem()
        }
    }

    private fun StrigaOccupation.mapItemToCellItem(): FinanceBlockCellModel {
        val leftSideCellModel = LeftSideCellModel.IconWithText(
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(toUiTitle(occupationName)),
                textAppearance = R.style.UiKit_TextAppearance_Regular_Text3,
                textColor = R.color.text_night
            ),
            icon = IconWrapperCellModel.SingleEmoji(emoji = emoji)
        )
        return FinanceBlockCellModel(
            leftSideCellModel = leftSideCellModel,
            payload = StrigaPresetDataItem.StrigaOccupationItem(this)
        )
    }

    private fun StrigaSourceOfFunds.mapItemToCellItem(): FinanceBlockCellModel {
        val leftSideCellModel = LeftSideCellModel.IconWithText(
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(toUiTitle(sourceName)),
                textAppearance = R.style.UiKit_TextAppearance_Regular_Text3,
                textColor = R.color.text_night
            )
        )
        return FinanceBlockCellModel(
            leftSideCellModel = leftSideCellModel,
            payload = StrigaPresetDataItem.StrigaSourceOfFundsItem(this)
        )
    }

    private fun Country.mapItemToCellItem(): FinanceBlockCellModel {
        val leftSideCellModel = LeftSideCellModel.IconWithText(
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(toUiTitle(name)),
                textAppearance = R.style.UiKit_TextAppearance_Regular_Text3,
                textColor = R.color.text_night
            ),
            icon = IconWrapperCellModel.SingleEmoji(emoji = flagEmoji)
        )
        return FinanceBlockCellModel(
            leftSideCellModel = leftSideCellModel,
            payload = StrigaPresetDataItem.StrigaCountryItem(this)
        )
    }

    fun toUiTitle(value: String): String {
        return value.replace("_", " ").lowercase().replaceFirstChar { it.uppercaseChar() }
    }

    @StringRes
    private fun getSelectedItemHeaderTitle(item: StrigaPresetDataItem): Int = when (item) {
        is StrigaPresetDataItem.StrigaCountryItem -> R.string.striga_chosen_country
        is StrigaPresetDataItem.StrigaOccupationItem -> R.string.striga_chosen
        is StrigaPresetDataItem.StrigaSourceOfFundsItem -> R.string.striga_chosen
    }

    @StringRes
    private fun getAllItemHeaderTitle(item: StrigaPresetDataItem): Int = when (item) {
        is StrigaPresetDataItem.StrigaCountryItem -> R.string.striga_all_countries
        is StrigaPresetDataItem.StrigaOccupationItem -> R.string.striga_all_industries
        is StrigaPresetDataItem.StrigaSourceOfFundsItem -> R.string.striga_all_sources
    }
}
