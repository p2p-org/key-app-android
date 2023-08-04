package org.p2p.wallet.striga.onramp.iban.presenter

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.InformerViewCellModel
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.SimpleMaskFormatter
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails

class StrigaUserIbanUiMapper {

    private val ibanMaskFormatter = SimpleMaskFormatter("#### #### #### #### #### #### #### #### #### ##")

    fun mapToCellModels(details: StrigaFiatAccountDetails): List<AnyCellItem> = buildList {
        this += ibanInfoModel(ibanMaskFormatter.format(details.iban))
        this += fiatCurrencyModel(details.currency)
        this += bicInfoModel(details.bic)
        this += ownerNameModel(details.bankAccountHolderName)
    }

    fun mapImportantNotesInformerModel(
        details: StrigaFiatAccountDetails
    ): InformerViewCellModel = InformerViewCellModel(
        leftIcon = InformerViewCellModel.LeftIconParams(
            icon = DrawableContainer(R.drawable.ic_alert),
            iconTint = R.color.night,
        ),
        caption = InformerViewCellModel.CaptionParams(
            value = TextContainer(R.string.striga_iban_informer_attention_caption, details.bankAccountHolderName),
            textColorRes = R.color.text_snow,
        ),
        backgroundDrawableRes = R.drawable.bg_rain_rounded_16,
        backgroundTintRes = R.color.bg_night,
    )

    fun mapImportantNotesCellModel(
        @StringRes textRes: Int,
        @DrawableRes iconRes: Int
    ): MainCellModel = MainCellModel(
        leftSideCellModel = LeftSideCellModel.IconWithText(
            icon = IconWrapperCellModel.SingleIcon(
                sizeDp = 24,
                icon = ImageViewCellModel(
                    icon = DrawableContainer(iconRes),
                    iconTint = R.color.night,
                )
            ),
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(textRes),
                textAppearance = R.style.UiKit_TextAppearance_Regular_Text3,
                maxLines = 100,
                ellipsize = null,
            ),
        )
    )

    private fun copyIconModel(): RightSideCellModel.IconWrapper =
        IconWrapperCellModel.SingleIcon(
            ImageViewCellModel(
                DrawableContainer.invoke(R.drawable.ic_copy_outlined),
                iconTint = R.color.icons_mountain
            )
        ).let(RightSideCellModel::IconWrapper)

    private fun ibanInfoModel(iban: String): MainCellModel =
        MainCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextContainer(R.string.striga_iban_account_iban),
                secondLineText = TextContainer(iban)
            ),
            rightSideCellModel = copyIconModel(),
            payload = iban
        )

    private fun fiatCurrencyModel(currency: String): MainCellModel =
        MainCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextContainer(R.string.striga_iban_account_currency),
                secondLineText = TextContainer(currency.uppercase())
            ),
            rightSideCellModel = null,
        )

    private fun bicInfoModel(bic: String): MainCellModel =
        MainCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextContainer(R.string.striga_iban_account_bic),
                secondLineText = TextContainer(bic)
            ),
            rightSideCellModel = copyIconModel(),
            payload = bic
        )

    private fun ownerNameModel(bankAccountHolderName: String): MainCellModel =
        MainCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextContainer(R.string.striga_iban_account_beneficiary),
                secondLineText = TextContainer(bankAccountHolderName)
            ),
            rightSideCellModel = copyIconModel(),
            payload = bankAccountHolderName
        )
}
