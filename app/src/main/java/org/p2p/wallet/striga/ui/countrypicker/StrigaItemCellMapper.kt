package org.p2p.wallet.striga.ui.countrypicker

import androidx.annotation.StringRes
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.striga.signup.model.StrigaOccupation
import org.p2p.wallet.striga.signup.model.StrigaSourceOfFunds
import org.p2p.wallet.striga.signup.model.StrigaPickerItem

class StrigaItemCellMapper {

    fun mapItemToCellItem(item: StrigaPickerItem): FinanceBlockCellModel? {
        return when (item) {
            is StrigaPickerItem.CountryItem -> item.selectedItem?.let { mapItemToCellItem(it) }
            is StrigaPickerItem.FundsItem -> item.selectedItem?.let { mapItemToCellItem(it) }
            is StrigaPickerItem.OccupationItem -> item.selectedItem?.let { mapItemToCellItem(it) }
        }
    }

    fun mapItemToCellItem(item: StrigaOccupation): FinanceBlockCellModel {
        val leftSideCellModel = LeftSideCellModel.IconWithText(
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(toUiTitle(item.occupationName)),
                textAppearance = R.style.UiKit_TextAppearance_Regular_Text3,
                textColor = R.color.text_night
            ),
            icon = IconWrapperCellModel.SingleEmoji(emoji = item.emoji)
        )
        return FinanceBlockCellModel(
            leftSideCellModel = leftSideCellModel,
            payload = StrigaPickerItem.OccupationItem(item)
        )
    }

    fun mapItemToCellItem(item: StrigaSourceOfFunds): FinanceBlockCellModel {
        val leftSideCellModel = LeftSideCellModel.IconWithText(
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(toUiTitle(item.sourceName)),
                textAppearance = R.style.UiKit_TextAppearance_Regular_Text3,
                textColor = R.color.text_night
            )
        )
        return FinanceBlockCellModel(
            leftSideCellModel = leftSideCellModel,
            payload = StrigaPickerItem.FundsItem(item)
        )
    }

    fun mapItemToCellItem(item: Country): FinanceBlockCellModel {
        val leftSideCellModel = LeftSideCellModel.IconWithText(
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(toUiTitle(item.name)),
                textAppearance = R.style.UiKit_TextAppearance_Regular_Text3,
                textColor = R.color.text_night
            ),
            icon = IconWrapperCellModel.SingleEmoji(emoji = item.flagEmoji)
        )
        return FinanceBlockCellModel(
            leftSideCellModel = leftSideCellModel,
            payload = StrigaPickerItem.CountryItem(item)
        )
    }

    fun toUiTitle(value: String): String {
        return value.replace("_", " ").lowercase().replaceFirstChar { it.uppercaseChar() }
    }

    @StringRes
    fun getSearchTitleResId(item: StrigaPickerItem): Int {
        return when (item) {
            is StrigaPickerItem.CountryItem -> R.string.striga_country
            is StrigaPickerItem.FundsItem -> R.string.striga_source_of_funds
            is StrigaPickerItem.OccupationItem -> R.string.striga_occupation_industry
        }
    }

    @StringRes
    fun getSelectedItemHeaderTitle(item: StrigaPickerItem): Int {
        return when (item) {
            is StrigaPickerItem.CountryItem -> R.string.striga_chosen_country
            is StrigaPickerItem.FundsItem -> R.string.striga_chosen
            is StrigaPickerItem.OccupationItem -> R.string.striga_chosen
        }
    }
    @StringRes
    fun getAllItemHeaderTitle(item: StrigaPickerItem): Int {
        return when (item) {
            is StrigaPickerItem.CountryItem -> R.string.striga_all_countries
            is StrigaPickerItem.FundsItem -> R.string.striga_all_industries
            is StrigaPickerItem.OccupationItem -> R.string.striga_all_sources
        }
    }
}
