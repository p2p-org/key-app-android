package org.p2p.wallet.striga.signup.presetpicker.mapper

import androidx.annotation.StringRes
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.finance_block.MainCellStyle
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.organisms.sectionheader.SectionHeaderCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaPresetDataItem
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaOccupation
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaSourceOfFunds

class StrigaItemCellMapper {

    fun buildCellModels(items: List<StrigaPresetDataItem>, selectedItem: StrigaPresetDataItem?): List<AnyCellItem> {
        return buildList {
            if (selectedItem?.getName()?.isNotEmpty() == true) {
                this += buildHeaderCellItem(getSelectedItemHeaderTitle(selectedItem))
                mapItemToCellItem(selectedItem)?.let { add(it) }
            }
            this += buildHeaderCellItem(getAllItemHeaderTitle(items.first()))
            this += items.mapNotNull { mapItemToCellItem(it) }
        }
    }

    fun buildSearchCellModels(
        items: List<StrigaPresetDataItem>,
        selectedItem: StrigaPresetDataItem
    ): List<AnyCellItem> {
        return buildList {
            this += buildHeaderCellItem(getAllItemHeaderTitle(selectedItem))
            this += items.mapNotNull { mapItemToCellItem(it) }
        }
    }

    private fun buildHeaderCellItem(@StringRes header: Int): SectionHeaderCellModel = SectionHeaderCellModel(
        sectionTitle = TextContainer(header),
        isShevronVisible = false,
        textColor = R.color.text_mountain,
        backgroundColor = R.color.bg_smoke,
        textAppearance = R.style.UiKit_TextAppearance_Regular_Caps
    )

    private fun mapItemToCellItem(item: StrigaPresetDataItem): MainCellModel? {
        return when (item) {
            is StrigaPresetDataItem.Country -> item.details?.mapItemToCellItem()
            is StrigaPresetDataItem.SourceOfFunds -> item.details?.mapItemToCellItem()
            is StrigaPresetDataItem.Occupation -> item.details?.mapItemToCellItem()
        }
    }

    private fun StrigaOccupation.mapItemToCellItem(): MainCellModel {
        val leftSideCellModel = LeftSideCellModel.IconWithText(
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(toUiTitle(occupationName)),
                textAppearance = R.style.UiKit_TextAppearance_Regular_Text3,
                textColor = R.color.text_night
            ),
            icon = IconWrapperCellModel.SingleEmoji(emoji = emoji)
        )
        return MainCellModel(
            leftSideCellModel = leftSideCellModel,
            payload = StrigaPresetDataItem.Occupation(this),
            styleType = MainCellStyle.BASE_CELL
        )
    }

    private fun StrigaSourceOfFunds.mapItemToCellItem(): MainCellModel {
        val leftSideCellModel = LeftSideCellModel.IconWithText(
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(toUiTitle(sourceName)),
                textAppearance = R.style.UiKit_TextAppearance_Regular_Text3,
                textColor = R.color.text_night
            )
        )
        return MainCellModel(
            leftSideCellModel = leftSideCellModel,
            payload = StrigaPresetDataItem.SourceOfFunds(this),
            styleType = MainCellStyle.BASE_CELL
        )
    }

    private fun CountryCode.mapItemToCellItem(): MainCellModel {
        val leftSideCellModel = LeftSideCellModel.IconWithText(
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(toUiTitle(countryName)),
                textAppearance = R.style.UiKit_TextAppearance_Regular_Text3,
                textColor = R.color.text_night
            ),
            icon = IconWrapperCellModel.SingleEmoji(emoji = flagEmoji)
        )
        return MainCellModel(
            leftSideCellModel = leftSideCellModel,
            payload = StrigaPresetDataItem.Country(this),
            styleType = MainCellStyle.BASE_CELL
        )
    }

    fun toUiTitle(value: String): String {
        return value.replace("_", " ")
            .lowercase()
            .replaceFirstChar { it.uppercaseChar() }
    }

    fun toUiTitleWithEmoji(emoji: String, value: String): String {
        return "$emoji ${toUiTitle(value)}"
    }

    fun fromUiTitle(value: String): String {
        return value.split(" ").joinToString(separator = "_", transform = String::uppercase)
    }

    @StringRes
    private fun getSelectedItemHeaderTitle(item: StrigaPresetDataItem): Int = when (item) {
        is StrigaPresetDataItem.Country -> R.string.striga_chosen_country
        is StrigaPresetDataItem.Occupation -> R.string.striga_chosen
        is StrigaPresetDataItem.SourceOfFunds -> R.string.striga_chosen
    }

    @StringRes
    private fun getAllItemHeaderTitle(item: StrigaPresetDataItem): Int = when (item) {
        is StrigaPresetDataItem.Country -> R.string.striga_all_countries
        is StrigaPresetDataItem.Occupation -> R.string.striga_all_industries
        is StrigaPresetDataItem.SourceOfFunds -> R.string.striga_all_sources
    }
}
